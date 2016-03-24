package io.landysh.inflor.java.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.ejml.data.DenseMatrix64F;
import static org.ejml.ops.CommonOps.invert;



public class EventFrame {
	//file details
	public String 						UUID;
	public Hashtable<String, String> 	keywords;
	public ArrayList<String> 			parameterList;
	public Hashtable<String, double[]> 	columnStore;
	
	// Compensation details
	public String[]						compParameters;
	public Integer[]					compParameterMap;
	public double[][]					FCSpillMatrix;
	public DenseMatrix64F				compMatrix;

	
	// fcs properties
	public Integer 						parameterCount;
	public Integer						eventCount;

	
	public EventFrame(Hashtable<String, String> newKeywords) throws Exception {
		keywords = newKeywords;
		parameterCount = getKeywordValueInteger("$PAR");
		eventCount = getKeywordValueInteger("$TOT");
		if (keywords.keySet().contains("$SPILL")||keywords.keySet().contains("SPILLOVER")){
			parseSpillover(keywords);
			compMatrix = new DenseMatrix64F(FCSpillMatrix);
			invert(compMatrix);
		}
		
		if(!validateHeader()){
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

	public String[] getCannonColumnNames() {
		String[] columnNames = new String[parameterCount];
		for (int i=0;i<parameterCount;i++){
			String PnNValue = keywords.get("$P" + (i+1) + "N");
				columnNames[i] = (PnNValue).trim();
		}
		return columnNames;
	}
	
	public String[] getDisplayColumnNames() {
		String[] columnNames = new String[parameterCount];
		for (int i=0;i<parameterCount;i++){
			String PnNValue = keywords.get("$P" + (i+1) + "N");
			String PnSValue = keywords.get("$P" + (i+1) + "S");
			if(!PnNValue.equals(PnSValue) && PnSValue!=null ){
				columnNames[i] = (PnNValue + "   " + PnSValue).trim();
			} else {
				columnNames[i] = PnNValue.trim();
			}		}
		return columnNames;
	}
	public Hashtable<String, double[]> getColumnData() {
		return columnStore;
	}

	public int findIndexByName(String s) throws Exception{
		Integer index = null;
		for (int i=0; i<parameterList.size();i++){
			if (parameterList.get(i).equals(s)){
				index = i;
			} 
		}
		if (index!= null){
			return index;
		} else {
			throw new Exception("Parameter index not found.");
		}
	}
	
	private void parseSpillover(Hashtable<String, String> keywords) 
			throws Exception {
		String spill = null;
		
		//Check for spillover keywords
		if(keywords.containsKey("$SPILLOVER")){
			spill = keywords.get("SPILLOVER");
		} else if (keywords.containsKey("SPILL")){
			spill = keywords.get("SPILL");
		} else {
			throw new Exception("No spillover keyword found.");
		}
		
		// Magic string parsing from FCS Spec PDF
		String[] s = spill.split(",");
		int p = Integer.parseInt(s[0].trim());
		double[][] matrix = new double[p][p];
		if (p >= 2){
			String[] compPars = new String[p];
			for(int i=0;i<compPars.length;i++){
				compPars[i] = s[i+1];
				double[] row = new double[p];
				for (int j=0;j<p;j++){
					int index = 1 + p + i*j+j;
					row[j] = Double.parseDouble(s[index]);	
				}
				matrix[i] = row;
			}
		compParameters = compPars;
		FCSpillMatrix = matrix;
		}else {
			throw new Exception("Spillover Keyword - " + spill + " - appears to be invalid.");
		}
	}
	public double[] getCompRow(double[] FCSRow) throws Exception {
		double[] compRow = null;
		if (compParameters!= null){
			compRow = new double[compParameters.length];
			for (int i=0;i<compParameters.length;i++){
				double spills = compMatrix.get(0,0);
				Integer pIndex = findIndexByName(compParameters[i]);
				double pValue = FCSRow[pIndex];
				for (int j=0;j< compParameters.length;j++){
					if (i!=j){
						Integer sIndex = findIndexByName(compParameters[j]);
						double sValue =  FCSRow[sIndex];
					}
				}
				
				compRow[i] = pValue;
			}
		}
		return compRow;
	}
}
