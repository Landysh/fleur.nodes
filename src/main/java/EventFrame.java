package main.java;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class EventFrame {
	public Hashtable<String, String> 	keywords;
	public ArrayList<String> 			parameterList;
	public Hashtable<String, double[]> 	columnStore;
	
	// fcs properties
	public Integer 						parameterCount;
	public Integer						eventCount;
	
	
	public EventFrame(Hashtable<String, String> newKeywords) throws Exception {
		keywords = newKeywords;
		parameterCount = getKeywordValueInteger("$PAR");
		eventCount = getKeywordValueInteger("$TOT");
		Boolean ok = false;
		ok = validateHeader();
		if(!ok){
			Exception e = new Exception("Invalid FCS Header.");
			e.printStackTrace();
			throw e;
		}
	}

	private Boolean validateHeader() {
		Boolean validHeader = false;
		// Check all required keywords later...
		if (getKeywordValueString("FCSVersion").contains("FCS") && getKeywordValueInteger("$TOT")!=null) {
			validHeader = true;
		} else {
			System.out.println("Invalid header: missing required information.");
		}

	return validHeader;
	}
	
	public Integer getKeywordValueInteger(String keyword) {
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

	public String getKeywordValueString(String keyword) {
		//This method will try to return an FCS header keyword and if it isn't found will return an empty string.
		String value = "";
		try{
			value = keywords.get(keyword).trim();
		}catch (NoSuchElementException e){
			System.out.println( keyword + " not found, <empy string> returned at your peril.");
		}
		return value;
	}
	
	public Hashtable<String, String> getHeader() {
		return keywords;
	}

	public void  setData(Hashtable<String, double[]> allData) {
		 columnStore = allData;
	}

	public String[] getColumnNames() {
		String[] columnNames = new String[parameterCount];
		for (int i=0;i<parameterCount;i++){
			String PnNValue = keywords.get("$P" + (i+1) + "N");
			String PnSValue = keywords.get("$P" + (i+1) + "S");
			if(!PnNValue.equals(PnSValue) && PnSValue!=null ){
				columnNames[i] = (PnNValue + " _ " + PnSValue).trim();
			} else {
				columnNames[i] = PnNValue.trim();
			}
		}
		return columnNames;
	}
}
