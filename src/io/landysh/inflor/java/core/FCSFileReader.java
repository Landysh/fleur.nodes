package io.landysh.inflor.java.core;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class FCSFileReader {

	// From Table 1 of FCS3.1 Spec. ANALYSIS and OTHER segments ignored.
	static final int BEGIN_FCSVersionOffset = 0;
	static final int END_FCSVersionOffset = 5;

	static final int FIRSTBYTE_BeginTextOffset = 10;
	static final int LASTBYTE_BeginTextOffset = 17;
	static final int FIRSTBYTE_EndTextOffset = 18;
	static final int LASTBYTE_EndTextOffset = 25;

	static final int FIRSTBYTE_BeginDataOffset = 26;
	static final int LASTBYTE_BeginDataOffset = 33;
	static final int FIRSTBYTE_EndDataOffset = 34;
	static final int LASTBYTE_EndDataOffset = 41;

	// file properties
	public String 				pathToFile;
	RandomAccessFile 			FCSFile;
	public Integer 				beginText;
	public Integer 				endText;
	public Integer 				beginData;
	public Integer 				endData;
	public String 				dataType;
	public Integer[] 			bitMap;
	EventFrame			 		eventFrame;
	public String				UUID;

	// Constructor
	public FCSFileReader(String path_to_file) throws Exception {
		pathToFile = path_to_file;
		File f = new File(pathToFile);
		// file specific properties
		FCSFile = new RandomAccessFile(f, "r");
		// text specific properties
		beginText = readOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset);
		endText = readOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset);
		Hashtable<String, String> header = readHeader(path_to_file);
		header.put("FCSVersion", readFCSVersion(FCSFile));
		eventFrame = new EventFrame(header);
		// data specific properties
		beginData = readOffset(FIRSTBYTE_BeginDataOffset, LASTBYTE_BeginDataOffset);
		endData = readOffset(FIRSTBYTE_EndDataOffset, LASTBYTE_EndDataOffset);
		bitMap = createBitMap(header);
		dataType = eventFrame.getKeywordValueString("$DATATYPE");
	}

	public void close() throws IOException {
		FCSFile.close();
	}

	private Integer[] createBitMap(Hashtable<String, String> keywords) {
		// This method reads how many bytes per parameter and returns an integer
		// array of these values
		Integer[] map = new Integer[eventFrame.parameterCount];
		for (int i = 1; i <= eventFrame.parameterCount; i++) {
			String key = "$P" + (i) + "B";
			Integer byteSize = eventFrame.getKeywordValueInteger(key);
			map[i - 1] = byteSize;
		}
		return map;
	}
	
	public void initRowReader() {
		try {
			FCSFile.seek(beginData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public String readFCSVersion(RandomAccessFile raFile)
			throws UnsupportedEncodingException, IOException, FileNotFoundException {
		// mark the current location (should be byte 0)
		FCSFile.seek(0);
		byte[] bytes = new byte[END_FCSVersionOffset - BEGIN_FCSVersionOffset+1];
		raFile.read(bytes);
		String FCSVersion = new String(bytes, "UTF-8");
		return FCSVersion;
	}

	private double[] readFloatRow(double[] row) throws IOException {
		for (int i=0; i<row.length; i++) {
			byte[] bytes = new byte[bitMap[i] /8 ];
			FCSFile.read(bytes);
			row[i] = ByteBuffer.wrap(bytes).getFloat();
		}
		return row;
	}
	private double[] readIntegerRow(double[] row) throws IOException {
		for (int i=0; i<row.length; i++) {
			Short I = null;
			byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
				I = ByteBuffer.wrap(bytes).getShort();
			row[i] = (float) I;
		}
		return row;
	}

	private Hashtable<String, String> readHeader(String path_to_file) throws IOException {
		Hashtable <String, String> header = new Hashtable<String, String>();
		
		// Delimiter is first UTF-8 character in the text section
		byte[] delimiterBytes = new byte[1];
		FCSFile.seek(beginText);
		FCSFile.read(delimiterBytes);
		String delimiter = new String(delimiterBytes);
		
		// Read the rest of the text bytes, this will contain the keywords
		// commonly referred to as the FCS header
		int textLength = endText - beginText + 1;
		byte[] keywordBytes = new byte[textLength];
		FCSFile.read(keywordBytes);
		String rawKeywords = new String(keywordBytes, "UTF-8");
		if (rawKeywords.length() > 0 && rawKeywords.charAt(rawKeywords.length() - 1) == delimiter.charAt(0)) {
			rawKeywords = rawKeywords.substring(0, rawKeywords.length() - 1);
		}
		// TODO catch case of delimiter in text file here.
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
		header = table;

		return header;
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
		// Hope it's pointed at the right spot!
		double[] row = new double[eventFrame.parameterCount];
		if (dataType.equals("F")) {
			row = readFloatRow(row);
			
		}else if (dataType.equals("I")){
			row = readIntegerRow(row);
		}
		return row;
	}

	public Hashtable<String, String> getHeader() {
		return eventFrame.getHeader();
	}

	public EventFrame getEventFrame() {
		return eventFrame;
	}

	public void readColumnEventData() throws IOException {
		Hashtable<String, double[]> allData = new Hashtable<String, double[]>();
		String[] columnNames = eventFrame.getCannonColumnNames();
		FCSFile.seek(beginData);
		for (int i=0; i< columnNames.length; i++){
			double[] column = new double[eventFrame.eventCount];
			allData.put(columnNames[i], column);
		}
		for (int i=0; i<eventFrame.eventCount; i++){
			double[] row = readRow();
			for (int j=0; j<columnNames.length;j++){
				allData.get(columnNames[j])[i] = row[j];
			}
		}
		eventFrame.setData(allData);
	}

	public Hashtable<String, double[]> getColumnStore() {
		return eventFrame.columnStore;
	}
}
