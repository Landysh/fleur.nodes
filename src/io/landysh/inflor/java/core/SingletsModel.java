package io.landysh.inflor.java.core;

import java.util.ArrayList;
import java.util.Hashtable;

public class SingletsModel {
	
	String[] 					 modelInputColumnNames 	= null;
	String[][] 					 modelInputVectorSets 	= null;
	Hashtable <String, double[]> modelVectors 			= null;
	
	public SingletsModel(String[] columnNames) {
		modelInputColumnNames = findUsableColumnNames(columnNames);
		modelInputVectorSets  = createVectorSets(modelInputColumnNames);
	}
	
	public String[] getModelColumnNames() {
		return modelInputColumnNames;
	}
	
	private String[][] createVectorSets(String[] inputNames) {
		// This method re-organizes any potentially interesting column names into groups for subsequent iteration.
		int masterParameterCount = 0;
		for (int i=0;i<inputNames.length;i++){
			// TODO the interesting part.
		}
		String[][] vectorSets = new String[masterParameterCount][3];
		
		return vectorSets;
	}

	private String[] findUsableColumnNames(String[] columnNames) {
		ArrayList <String> scatterParameters = new ArrayList<String>();		
		String[] scatterRegexList = {"ssc*","fsc*","*scatter*"};
		for (int i=0;i<columnNames.length;i++){
			for (int j=0;i<scatterRegexList.length;j++){
				if (columnNames[i].matches(scatterRegexList[j])){
					scatterParameters.add(columnNames[i]);
				}
			}
		}
		final String[] usableColumnNames = (String[]) scatterParameters.toArray();
		return usableColumnNames;
	}
}
