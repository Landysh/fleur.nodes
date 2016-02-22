package org.JOSC;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.StringTokenizer;

public class FCSFileReader {
	
//	From Table 1 of FCS3.1 Spec.  ANALYSIS and OTHER segments ignored.
	
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

	public static Hashtable<String, String> getFCSHeader(String pathToFile){
		File file = new File(pathToFile);
		Hashtable<String, String> keywords = new Hashtable<String, String>();

		if(isValidFCS3(pathToFile)){
			RandomAccessFile raFile;
			try {
				raFile = new RandomAccessFile(file,"r");
				int beginTextOffset = getOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset, raFile);
				int endTextOffset = getOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset, raFile);
				//Delimiter is first UTF-8 character in the text section
				byte[] delimiterBytes = new byte[1];
				raFile.seek(beginTextOffset);
				raFile.read(delimiterBytes);
				String delimiter = new String(delimiterBytes);

				//Read the rest of the text bytes, this will contain the keywords commmonly refered to as the FCS header
				byte[] keywordBytes = new byte[endTextOffset - beginTextOffset];
				raFile.read(keywordBytes);
				String rawKeywords = new String(keywordBytes, "UTF-8");
//TODO catch case of delimiter in text file here. 
				StringTokenizer  s = new StringTokenizer(rawKeywords, delimiter);
				while (s.hasMoreTokens()) {
				    String key = s.nextToken();
				    String value = s.nextToken();
					keywords.put(key, value);				    
				}			
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
				
		return keywords;
	}
	
	public static Boolean isValidFCS3(String pathToFile){
		//check validity of fcs file according to spec keywords
		Boolean isValid = false;
		//Does the file exist?
		try {
			File file = new File(pathToFile);
			RandomAccessFile raFile = new RandomAccessFile(file, "r");
			//if so, check offsets and header. 
			Boolean validHeader = hasValidHeader(raFile);
			if (validHeader){
				isValid = true;
			} else {
				System.out.println("File header invalid.");
			}
			raFile.close();
		} catch (IOException e) {
			isValid = false;
			System.out.println("File does not exist or is unreadable");
			e.printStackTrace();

		}

		return isValid;
	}

	private static Boolean hasValidHeader(RandomAccessFile raFile) throws FileNotFoundException, IOException {
		Boolean validHeader = false;

		try{
			String FCSVersion 	= getFCSVersion(BEGIN_FCSVersionOffset, END_FCSVersionOffset, raFile);
//		Check required keywords later...
//		int beginTextOffset = getOffset(FIRSTBYTE_BeginTextOffset,LASTBYTE_BeginTextOffset,fs);
//		int endTextOffset 	= getOffset(FIRSTBYTE_EndTextOffset,LASTBYTE_EndTextOffset,fs);
			if (FCSVersion.contains("FCS3.")){
				validHeader = true;
			} else {
					System.out.println("FCS File version 3 not found, this is the only supported file type.");
			}
		} catch (UnsupportedEncodingException e){
			System.out.println("Invalid encoiding, only UTF-8 files are supported by the standard");
			e.printStackTrace();
		}
		return validHeader;
	}

	private static int getOffset(int start, int end, RandomAccessFile raFile) throws IOException {
		// +1?
		byte[] bytes = new byte[end - start + 1];
		raFile.seek(start);
		raFile.read(bytes);
		String s = new String(bytes, "UTF-8");
		int offSet = Integer.parseInt(s.trim());
		return offSet;
	}

	private static String getFCSVersion(int start, int end, RandomAccessFile raFile) 
			throws UnsupportedEncodingException, IOException, FileNotFoundException {
		//mark the current location (should be byte 0)
		byte[] bytes = new byte[end - start];
		raFile.read(bytes);
		String FCSVersion = new String(bytes, "UTF-8");
		return FCSVersion;
	}

	public static String getHeader(String pathToFile){
		//if File is valid, read the header
		return "foo";
	}
	
}
