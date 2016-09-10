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
import io.landysh.inflor.java.core.dataStructures.FCParameter;
import io.landysh.inflor.java.core.dataStructures.FCVectorType;
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
			final FCSFileReader reader = new FCSFileReader(filePath, false);
			isValid = true;
		} catch (final Exception e) {
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
		final File f = new File(pathToFile);
		FCSFile = new RandomAccessFile(f, "r");

		compensateOnRead = compensate;

		// text specific properties
		beginText = readOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset);
		endText = readOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset);
		final Hashtable<String, String> header = readHeader(path_to_file);
		header.put("FCSVersion", readFCSVersion(FCSFile));

		// Try to validate the header.
		if (FCSUtils.validateHeader(header) == false) {
			final Exception e = new Exception("Invalid FCS Header.");
			e.printStackTrace();
			throw e;
		}

		fileParameterList = FCSUtils.parseParameterList(header);
		if (compensate == true) {
			final SpilloverCompensator comp = new SpilloverCompensator(header);
			compParameterList = comp.getCompParameterNames();
		}

		final int rowCount = Integer.parseInt(header.get("$TOT"));
		columnStore = new ColumnStore(header, rowCount);

		// data specific properties
		beginData = readOffset(FIRSTBYTE_BeginDataOffset, LASTBYTE_BeginDataOffset);
		readOffset(FIRSTBYTE_EndDataOffset, LASTBYTE_EndDataOffset);
		bitMap = createBitMap(header);
		dataType = columnStore.getKeywordValue("$DATATYPE");
	}

	private String calculateSHA(byte[] inBytes) throws NoSuchAlgorithmException {
		StringBuffer buffer = null;
		final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(inBytes);

		final byte[] bytes = messageDigest.digest();
		buffer = new StringBuffer();
		for (final byte b : bytes) {
			buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
		}

		return buffer.toString();
	}

	public void close() throws IOException {
		FCSFile.close();
	}

	private Integer[] createBitMap(Hashtable<String, String> keywords) {
		// This method reads how many bytes per parameter and returns an integer
		// array of these values
		final String[] rawParameterNames = FCSUtils.parseParameterList(keywords);
		final Integer[] map = new Integer[rawParameterNames.length];
		for (int i = 1; i <= map.length; i++) {
			final String key = "$P" + (i) + "B";
			final String value = columnStore.getKeywordValue(key);
			final Integer byteSize = Integer.parseInt(value);
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
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

//	private String[] parseColumnNames(Hashtable<String, String> header, boolean compensate) {
//		if (compensate == true) {
//			try {
//				final SpilloverCompensator comp = new SpilloverCompensator(header);
//				final String[] compParameters = comp.getCompDisplayNames(header);
//				final String[] allParameters = new String[compParameters.length + fileParameterList.length];
//				for (int i = 0; i < fileParameterList.length; i++) {
//					allParameters[i] = fileParameterList[i];
//				}
//				for (int j = 0; j < compParameters.length; j++) {
//					allParameters[fileParameterList.length + j] = compParameters[j];
//				}
//				return allParameters;
//			} catch (final Exception e) {
//				return fileParameterList;
//			}
//
//		} else {
//			return fileParameterList;
//		}
//	}

	private Hashtable<String, FCParameter> readAndCompensateColumns(Hashtable<String, FCParameter> allData,
			SpilloverCompensator compensator) throws IOException {
		FCSFile.seek(beginData);
		for (int i = 0; i < columnStore.getRowCount(); i++) {
			final double[] row = readRow();
			final double[] compRow = compensator.compensateRow(row);
			for (int j = 0; j < fileParameterList.length; j++) {
				allData.get(fileParameterList[j]).getData(FCVectorType.RAW)[i] = row[j];
				for (int k = 0; k < compParameterList.length; k++) {
					if (compParameterList[k] == fileParameterList[j]) {
						allData.get(compParameterList[k]).getData(FCVectorType.COMP)[i] = compRow[k];
					}
				}
			}
		}
		return allData;
	}

	private Hashtable<String, FCParameter> readColumns(Hashtable<String, FCParameter> allData) throws IOException {
		for (int i = 0; i < columnStore.getRowCount(); i++) {
			final double[] row = readRow();
			for (int j = 0; j < fileParameterList.length; j++) {
				allData.get(fileParameterList[j]).getData(FCVectorType.RAW)[i]= row[j];
			}
		}
		return allData;
	}

	public void readData() throws Exception {
		Hashtable<String, FCParameter> allData = new Hashtable<String, FCParameter>();
		//final String[] vectorNames = columnStore.getColumnNames();
		FCSFile.seek(beginData);
		// Initialize vector store
		for (final String name : fileParameterList) {
			final FCParameter newParameter = new FCParameter(name, columnStore.getRowCount());
			allData.put(name, newParameter);
		}

		if (compensateOnRead == true) {
			try {
				final SpilloverCompensator compensator = new SpilloverCompensator(getHeader());
				allData = readAndCompensateColumns(allData, compensator);
			} catch (final Exception e) {
				allData = readColumns(allData);
			}

		} else {
			allData = readColumns(allData);
		}
		columnStore.setData(allData);
	}

	public String readFCSVersion(RandomAccessFile raFile)
			throws UnsupportedEncodingException, IOException, FileNotFoundException {
		// mark the current location (should be byte 0)
		FCSFile.seek(0);
		final byte[] bytes = new byte[END_FCSVersionOffset - BEGIN_FCSVersionOffset + 1];
		raFile.read(bytes);
		final String FCSVersion = new String(bytes, "UTF-8");
		return FCSVersion;
	}

	private double[] readFloatRow(double[] row) throws IOException {
		for (int i = 0; i < row.length; i++) {
			final byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
			row[i] = ByteBuffer.wrap(bytes).getFloat();
		}
		return row;
	}

	private Hashtable<String, String> readHeader(String path_to_file) throws Exception {
		Hashtable<String, String> header = new Hashtable<String, String>();

		// Delimiter is first UTF-8 character in the text section
		final byte[] delimiterBytes = new byte[1];
		FCSFile.seek(beginText);
		FCSFile.read(delimiterBytes);
		final String delimiter = new String(delimiterBytes);

		// Read the rest of the text bytes, this will contain the keywords
		final int textLength = endText - beginText + 1;
		final byte[] keywordBytes = new byte[textLength];

		FCSFile.read(keywordBytes);
		String rawKeywords = new String(keywordBytes, "UTF-8");
		if (rawKeywords.length() > 0 && rawKeywords.charAt(rawKeywords.length() - 1) == delimiter.charAt(0)) {
			rawKeywords = rawKeywords.substring(0, rawKeywords.length() - 1);
		}
		final StringTokenizer s = new StringTokenizer(rawKeywords, delimiter);
		final Hashtable<String, String> table = new Hashtable<String, String>();
		Boolean ok = true;
		while (s.hasMoreTokens() && ok) {
			final String key = s.nextToken().trim();
			if (key.trim().isEmpty()) {
				ok = false;
			} else {
				final String value = s.nextToken().trim();
				table.put(key, value);
			}
		}
		final String sha256 = calculateSHA(keywordBytes);
		table.put("SHA-256", sha256);
		header = table;
		return header;
	}

	private double[] readIntegerRow(double[] row) throws IOException {
		for (int i = 0; i < row.length; i++) {
			Short I = null;
			final byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
			I = ByteBuffer.wrap(bytes).getShort();
			row[i] = I;
		}
		return row;
	}

	private int readOffset(int start, int end) throws IOException {
		// +1?
		final byte[] bytes = new byte[end - start + 1];
		FCSFile.seek(start);
		FCSFile.read(bytes);
		final String s = new String(bytes, "UTF-8");
		final int offSet = Integer.parseInt(s.trim());
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