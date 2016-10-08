package io.landysh.inflor.java.core.utils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSDimension;

public class FCSUtils {

	public static Integer findParameterNumnberByName(HashMap<String, String> keywords, String name) {
		/**
		 * Attempts to find the parameter number in a supplied FCS header
		 * (keywords). If the parameter name is not it will return null.
		 */
		Integer parameterIndex = -1;
		final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));
		for (int i = 1; i <= parameterCount; i++) {
			final String parameterKey = "$P" + i + "N";
			final String value = keywords.get(parameterKey);
			if (value.matches(name)) {
				parameterIndex = i;
				break;
			}
		}
		if (parameterIndex != -1) {
			return parameterIndex;
		} else {
			return null;
		}
	}

	public static String findStainName(HashMap<String, String> keywords, Integer parameterIndex) {
		/**
		 * Returns the $PnS value for a particular parameter index
		 */
		String stainName = "";
		try {
			stainName = keywords.get("$P" + (1 + parameterIndex) + "S");
		} catch (final Exception e) {
			System.out.print("No stain name found for parameter index: " + parameterIndex);
		}
		return stainName;
	}

	public static double[] filterColumn(BitSet mask, double[] data) {
		double[] filteredData = new double[mask.cardinality()];
		int currentBit=0;
		for (int i=0;i<filteredData.length;i++){
			int nextBit =  mask.nextSetBit(currentBit);
			filteredData[i] = data[nextBit];
			currentBit = nextBit+1;
		}
		return filteredData;
	}
	
	public static String[] parseDimensionList(HashMap<String, String> keywords) {
		/**
		 * Returns a String[] containing all of the values of the $PnN keywords
		 * from the specified header table.
		 */
		final int columnCount = Integer.parseInt(keywords.get("$PAR"));
		final String[] plist = new String[columnCount];
		for (int i = 1; i <= columnCount; i++) {
			final String keyword = ("$P" + i + "N");
			plist[i - 1] = keywords.get(keyword);
		}
		return plist;
	}

	public static Boolean validateHeader(HashMap<String, String> keywords) {
		/**
		 * Returns a boolean indicating whether the specified keywords are
		 * consistent with the requirements of the FCS Standard version 3.1.
		 */
		boolean validHeader = false;
		// Check all required keywords later...
		final boolean fcsVersion = keywords.get("FCSVersion").contains("FCS");
		final Integer rowCount = Integer.parseInt(keywords.get("$TOT"));
		final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));
		if (fcsVersion == true && rowCount > 0 && parameterCount > 0) {
			validHeader = true;
		} else {
			System.out.println("Invalid header: missing required information.");
		}
		return validHeader;
	}

	public static HashMap<String, String> findParameterKeywords(HashMap <String, String> sourceKeywords, int parameterIndex) {
		HashMap <String, String> keywords = new HashMap<String, String>();
		String regex = "\\$P" + parameterIndex +"[A-Z]";
		for (String key:keywords.keySet()){
			if (key.matches(regex)){
				keywords.put(key, sourceKeywords.get(key));
			}
		}
		return keywords;
	}

	public static FCSDimension buildFCSDimension(int pIndex, HashMap<String, String> header, boolean wasComped) {
		/**
		 * Constructs a new FCSDimension object from the parameter data in the header of an FCS File.
		 */
		int size = Integer.parseInt(header.get("$TOT"));
		String pnn = header.get("$P" + pIndex + "N");
		String pns = header.get("$P" + pIndex + "S");
		String pne = header.get("$P" + pIndex + "E");
		double pneF1 = Double.parseDouble(pne.split(",")[0]);
		double pneF2 = Double.parseDouble(pne.split(",")[1]);
		double pnr = Double.parseDouble(header.get("$P" + pIndex + "R"));

		
		FCSDimension newDimension = new FCSDimension(size,
													 pIndex, 
													 pnn, 
													 pns, 
													 pneF1, 
													 pneF2, 
													 pnr,
													 wasComped);
		
		return newDimension;
	}

	public static ColumnStore filterColumnStore(BitSet mask, ColumnStore in) {
		
		ColumnStore out = new ColumnStore(in.getKeywords(), mask.cardinality());
		for (String name:in.getData().keySet()){
			FCSDimension  inDim = in.getData().get(name);
			FCSDimension outDim = new FCSDimension(mask.cardinality(), inDim.getIndex(), inDim.getShortName(), inDim.getDisplayName(), 
					inDim.getPNEF1(), inDim.getPNEF2(), inDim.getRange(), inDim.getCompRef());
			out.addColumn(name, outDim);
		}
		return out;
	}

	public static FCSDimension findCompatibleDimension(Map<String, FCSDimension> dimensionMap, String name) {
		/**
		 * Returns the key for the first compatible FCSDimension in the selected map.
		 * (ie. where the result of the toString() method is the same).
		 * Will return null if no compatible entry is found.
		 */
		FCSDimension returnDim = null;
		for (FCSDimension dim: dimensionMap.values()){
			if (dim.toString().equals(name)){
				returnDim = dim;
			}
		}
		return returnDim;
	}
}
