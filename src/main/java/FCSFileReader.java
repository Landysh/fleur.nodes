package main.java;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
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
	public String				FCSVersion;
	Hashtable<String, String> 	header;

	// fcs properties
	public Integer 				parameterCount;
	public Integer 				beginText;
	public Integer 				endText;
	public Integer 				beginData;
	public Integer 				endData;
	public String 				dataType;
	public Integer[] 			bitMap;
	public Long 				flowJoDemoID = null;

	// Constructor
	public FCSFileReader(String path_to_file) throws Exception {
		pathToFile = path_to_file;
		File f = new File(pathToFile);
		// file specific properties
		FCSFile = new RandomAccessFile(f, "r");
		FCSVersion = readFCSVersion(FCSFile);
		// text specific properties
		beginText = readOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset);
		endText = readOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset);
		header = readHeader(path_to_file);
		// data specific properties
		parameterCount = getKeywordValueInteger("$PAR", header);
		beginData = readOffset(FIRSTBYTE_BeginDataOffset, LASTBYTE_BeginDataOffset);
		endData = readOffset(FIRSTBYTE_EndDataOffset, LASTBYTE_EndDataOffset);
		bitMap = createBitMap(header);
		Boolean ok = hasValidHeader(header);
		dataType = getKeywordValueString("$DATATYPE", header);

		if(!ok){
			Exception e = new Exception("Invalid FCS Header: ");
			throw e;
		}
	}

	public void close() throws IOException {
		FCSFile.close();
	}

	private Integer[] createBitMap(Hashtable<String, String> keywords) {
		// This method reads how many bytes per parameter and returns an integer
		// array of these values
		Integer parCount = getKeywordValueInteger("$PAR", keywords);
		Integer[] map = new Integer[parCount];
		for (int i = 1; i <= parCount; i++) {
			String key = "$P" + (i) + "B";
			Integer byteSize = getKeywordValueInteger(key, keywords);
			map[i - 1] = byteSize;
		}
		return map;
	}

	public String getFCSVersion(){
		return FCSVersion;
	}
	
	public Hashtable<String, String> getHeader() {
		return header;
	}

	public static Integer getKeywordValueInteger(String keyword, Hashtable<String, String> keywords) {
		//This method will try to return an FCS header keyword and if it isn't found will return -1!
		Integer value = -1; 
		if(keywords.containsKey(keyword)){
			String valueString = keywords.get(keyword).trim();
			value = Integer.parseInt(valueString);
		} else {
			System.out.println( keyword + " not found, -1 returned at your peril.");
		}
		return value;
	}

	public static String getKeywordValueString(String keyword, Hashtable<String, String> keywords) {
		//This method will try to return an FCS header keyword and if it isn't found will return an empty string.
		String value = "";
		try{
			value = keywords.get(keyword).trim();
		}catch (NoSuchElementException e){
			System.out.println( keyword + " not found, <empy string> returned at your peril.");
		}
		return value;
	}
	
	public String[] getParameterNames(Hashtable<String, String> header){
		// provide the header of an FCS file Reader (eg. getHeader()) and get some back the parameter list.
		String[] parameterList = new String[getKeywordValueInteger("$PAR", header)];
		return parameterList;
	}
	
	
	private Boolean hasValidHeader(Hashtable<String, String> header) throws FileNotFoundException, IOException {
		Boolean validHeader = false;
		// Check required keywords later...
		if (FCSVersion.contains("FCS" )&& header.get("$TOT")!=null) {
			validHeader = true;
		} else {
			System.out.println("Invalid header: missing required information.");
		}

	return validHeader;
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
		byte[] bytes = new byte[END_FCSVersionOffset - BEGIN_FCSVersionOffset];
		raFile.read(bytes);
		String FCSVersion = new String(bytes, "UTF-8");
		return FCSVersion;
	}

	private double[] readFloatRow(double[] row) throws IOException {
		for (int i = 0; i <= row.length - 1; i++) {
			byte[] bytes = new byte[bitMap[i] /8 ];
			FCSFile.read(bytes);
			row[i] = ByteBuffer.wrap(bytes).getFloat();
		}
		return row;
	}
	private double[] readIntegerRow(double[] row) throws IOException {
		for (int i = 0; i <= row.length - 1; i++) {
			Short I = null;
			byte[] bytes = new byte[bitMap[i] / 8];
			FCSFile.read(bytes);
				I = ByteBuffer.wrap(bytes).getShort();
			row[i] = (float) I;
		}
		return row;
	}

	private Hashtable<String, String> readHeader(String path_to_file) throws IOException {
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
		// Hope it's pointed at the right spot! (use ) and also we have a reference to global variable
		double[] row = new double[parameterCount];
		if (dataType.equals("F")) {
			row = readFloatRow(row);
			
		}else if (dataType.equals("I")){
			row = readIntegerRow(row);
		}
		return row;
	}

	public double[][] readAllData() throws IOException {
		Integer cellCount = getKeywordValueInteger("$TOT", header);
		double [][] allData = new double[parameterCount][cellCount];
		FCSFile.seek(beginData);
		double[] row = new double[parameterCount];
		if (dataType.equals("F")){
			for (int i=0;i<cellCount;i++){
				row = readFloatRow(row);
				for (int j=0;j<row.length;j++){
					allData[i][j] = row[j];
				}
			}
		}else if (dataType.equals("F")){
			for (int i=0;i<cellCount;i++){
				row = readIntegerRow(row);
				for (int j=0;j<row.length;j++){
					allData[i][j] = row[j];
				}
			}
		}else {
			System.out.println("Houston, we have an unsupported data type (or some other problem).");
		}
		return allData;
	}
    
}
