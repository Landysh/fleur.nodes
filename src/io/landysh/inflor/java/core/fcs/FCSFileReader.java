package io.landysh.inflor.java.core.fcs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSVector;
import io.landysh.inflor.java.core.gatingML.compensation.SpilloverCompensator;
import io.landysh.inflor.java.core.utils.FCSUtils;

public class FCSFileReader {

	// From Table 1 of FCS3.1 Spec. ANALYSIS and OTHER segments ignored.
	private static final int BEGIN_FCSVersionOffset = 0;
	private static final int END_FCSVersionOffset = 5;

	private static final int FIRSTBYTE_BeginTextOffset = 10;
	private static final int LASTBYTE_BeginTextOffset = 17;
	private static final int FIRSTBYTE_EndTextOffset = 18;
	private static final int LASTBYTE_EndTextOffset = 25;

	private static final int FIRSTBYTE_BeginDataOffset = 26;
	private static final int LASTBYTE_BeginDataOffset = 33;
	private static final int FIRSTBYTE_EndDataOffset = 34;
	private static final int LASTBYTE_EndDataOffset = 41;

	public static boolean isValidFCS(String filePath) {
		boolean isValid = false;
		try {
			@SuppressWarnings("unused")
			FCSFileReader reader = new FCSFileReader(filePath, false);
			isValid = true;
		} catch (Exception e) {
			// noop
		}
		return isValid;
	}

	// file properties
	public final String pathToFile;
	public final RandomAccessFile FCSFile;
	public final Integer beginText;
	public final Integer endText;
	public final Integer beginData;
	public final String dataType;
	public final Integer[] bitMap;
	public final ColumnStore columnStore;
	public final String[] fileParameterList;
	private final boolean compensateOnRead;

	public String[] compParameterList = null;

	// Constructor
	public FCSFileReader(String path_to_file, boolean compensate) throws Exception {
		// Open the file
		pathToFile = path_to_file;
		File f = new File(pathToFile);
		FCSFile = new RandomAccessFile(f, "r");

		compensateOnRead = compensate;

		// text specific properties
		beginText = readOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset);
		endText = readOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset);
		Hashtable<String, String> header = readHeader(path_to_file);
		header.put("FCSVersion", readFCSVersion(FCSFile));

		// Try to validate the header.
		if (FCSUtils.validateHeader(header) == false) {
			Exception e = new Exception("Invalid FCS Header.");
			e.printStackTrace();
			throw e;
		}

		fileParameterList = FCSUtils.parseParameterList(header);
		if (compensate == true) {
			SpilloverCompensator comp = new SpilloverCompensator(header);
			compParameterList = comp.getCompParameterNames();
		}

		int rowCount = Integer.parseInt(header.get("$TOT"));
		String[] columnNames = parseColumnNames(header, compensate);
		columnStore = new ColumnStore(header, columnNames);
		columnStore.setRowCount(rowCount);

		// data specific properties
		beginData = readOffset(FIRSTBYTE_BeginDataOffset, LASTBYTE_BeginDataOffset);
		readOffset(FIRSTBYTE_EndDataOffset, LASTBYTE_EndDataOffset);
		bitMap = createBitMap(header);
		dataType = columnStore.getKeywordValue("$DATATYPE");
	}

	private String calculateSHA(byte[] inBytes) {
		/**
		 * Returns the SHA256 checksum of a byte array or the literal string
		 * "Error" in the case of an exception being thrown during execution
		 */
		StringBuffer buffer = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(inBytes);

			byte[] bytes = messageDigest.digest();
			buffer = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				buffer.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

		} catch (NoSuchAlgorithmException e) {
			// Should never happen in this context. Algorithm is hard coded.
			e.printStackTrace();
		}
		if (buffer != null) {
			return buffer.toString();
		} else {
			throw new NullPointerException("Could not calculate SHA, byte buffer is null");
		}
	}

	public void close() throws IOException {
		FCSFile.close();
	}

	private Integer[] createBitMap(Hashtable<String, String> keywords) {
		// This method reads how many bytes per parameter and returns an integer
		// array of these values
		String[] rawParameterNames = FCSUtils.parseParameterList(keywords);
		Integer[] map = new Integer[rawParameterNames.length];
		for (int i = 1; i <= map.length; i++) {
			String key = "$P" + (i) + "B";
			String value = columnStore.getKeywordValue(key);
			Integer byteSize = Integer.parseInt(value);
			map[i - 1] = byteSize;
		}
		return map;
	}

	public ColumnStore getColumnStore() {
		return columnStore;
	}

	public Hashtable<String, String> getHeader() {
		return columnStore.getKeywords();
	}

	public boolean hasCompParameters() {
		if (compParameterList != null && compParameterList.length >= 2) {
			return true;
		} else {
			return false;
		}
	}

	public void initRowReader() {
		try {
			FCSFile.seek(beginData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String[] parseColumnNames(Hashtable<String, String> header, boolean compensate) {
		if (compensate == true) {
			try {
				SpilloverCompensator comp = new SpilloverCompensator(header);
				String[] compParameters = comp.getCompDisplayNames(header);
				String[] allParameters = new String[compParameters.length + fileParameterList.length];
				for (int i = 0; i < fileParameterList.length; i++) {
					allParameters[i] = fileParameterList[i];
				}
				for (int j = 0; j < compParameters.length; j++) {
					allParameters[fileParameterList.length + j] = compParameters[j];
				}
				return allParameters;
			} catch (Exception e) {
				return fileParameterList;
			}

		} else {
			return fileParameterList;
		}
	}

	private Hashtable<String, FCSVector> readAndCompensateColumns(Hashtable<String, FCSVector> allData,
			SpilloverCompensator compensator) throws IOException {
		FCSFile.seek(beginData);
		for (int i = 0; i < columnStore.getRowCount(); i++) {
			double[] row = readRow();
			double[] compRow = compensator.compensateRow(row);
			for (int j = 0; j < fileParameterList.length; j++) {
				allData.get(fileParameterList[j]).setRawValue(i, row[j]);
				for (int k = 0; k < compParameterList.length; k++) {
					if (compParameterList[k] == fileParameterList[j]) {
						allData.get(compParameterList[k]).setCompValue(i, compRow[k]);
					}
				}
			}
		}
		return allData;
	}

	public void readData() throws Exception {
		Hashtable<String, FCSVector> allData = new Hashtable<String, FCSVector>();
		String[] vectorNames = columnStore.getColumnNames();
		// Initialize vector store
		for (String name : vectorNames) {
			FCSVector vector = new FCSVector(name);
			vector.setSize(columnStore.getRowCount());
			allData.put(name, vector);
		}

		if (compensateOnRead == true) {
			try {
				SpilloverCompensator compensator = new SpilloverCompensator(getHeader());
				allData = readAndCompensateColumns(allData, compensator);
			} catch (Exception e) {
				allData = readColumns(allData);
			}

		} else {
			allData = readColumns(allData);
		}
		columnStore.setData(allData);
	}

	private Hashtable<String, FCSVector> readColumns(Hashtable<String, FCSVector> allData) throws IOException {
		for (int i = 0; i < columnStore.getRowCount(); i++) {
			double[] row = readRow();
			for (int j = 0; j < fileParameterList.length; j++) {
				allData.get(fileParameterList[j]).setRawValue(i, row[j]);
			}
		}
		return allData;
	}

	public String readFCSVersion(RandomAccessFile raFile)
			throws UnsupportedEncodingException, IOException, FileNotFoundException {
		// mark the current location (should be byte 0)
		FCSFile.seek(0);
		byte[] bytes = new byte[END_FCSVersionOffset - BEGIN_FCSVersionOffset + 1];
		raFile.read(bytes);
		String FCSVersion = new String(bytes, "UTF-8");
		return FCSVersion;
	}

	private double[] readFloatRow(double[] row) throws IOException {
		for (int i = 0; i < row.length; i++) {
			byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
			row[i] = ByteBuffer.wrap(bytes).getFloat();
		}
		return row;
	}

	private Hashtable<String, String> readHeader(String path_to_file) throws Exception {
		Hashtable<String, String> header = new Hashtable<String, String>();

		// Delimiter is first UTF-8 character in the text section
		byte[] delimiterBytes = new byte[1];
		FCSFile.seek(beginText);
		FCSFile.read(delimiterBytes);
		String delimiter = new String(delimiterBytes);

		// Read the rest of the text bytes, this will contain the keywords
		int textLength = endText - beginText + 1;
		byte[] keywordBytes = new byte[textLength];

		FCSFile.read(keywordBytes);
		String rawKeywords = new String(keywordBytes, "UTF-8");
		if (rawKeywords.length() > 0 && rawKeywords.charAt(rawKeywords.length() - 1) == delimiter.charAt(0)) {
			rawKeywords = rawKeywords.substring(0, rawKeywords.length() - 1);
		}
		StringTokenizer s = new StringTokenizer(rawKeywords, delimiter);
		Hashtable<String, String> table = new Hashtable<String, String>();
		Boolean ok = true;
		while (s.hasMoreTokens() && ok) {
			String key = s.nextToken().trim();
			if (key.trim().isEmpty()) {
				ok = false;
			} else {
				String value = s.nextToken().trim();
				table.put(key, value);
			}
		}
		String sha256 = calculateSHA(keywordBytes);
		table.put("SHA-256", sha256);
		header = table;
		return header;
	}

	private double[] readIntegerRow(double[] row) throws IOException {
		for (int i = 0; i < row.length; i++) {
			Short I = null;
			byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
			I = ByteBuffer.wrap(bytes).getShort();
			row[i] = I;
		}
		return row;
	}

	private int readOffset(int start, int end) throws IOException {
		// +1?
		byte[] bytes = new byte[end - start + 1];
		FCSFile.seek(start);
		FCSFile.read(bytes);
		String s = new String(bytes, "UTF-8");
		int offSet = Integer.parseInt(s.trim());
		return offSet;
	}

	public double[] readRow() throws IOException {
		/**
		 * Reads the next row of the data.
		 */

		double[] row = new double[fileParameterList.length];
		if (dataType.equals("F")) {
			row = readFloatRow(row);

		} else if (dataType.equals("I")) {
			row = readIntegerRow(row);
		}
		return row;
	}
}