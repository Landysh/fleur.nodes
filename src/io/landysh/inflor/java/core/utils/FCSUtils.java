package io.landysh.inflor.java.core.utils;

import java.util.Hashtable;

public class FCSUtils {

	public static Integer findParameterNumnberByName(Hashtable<String, String> keywords, String name) {
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

	public static String findStainName(Hashtable<String, String> keywords, Integer parameterIndex) {
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

	public static String getDisplayName(Hashtable<String, String> keywords, String name, boolean compensated) {
		final int parameterIndex = findParameterNumnberByName(keywords, name);
		String displayName = name;
		final String stainName = findStainName(keywords, parameterIndex);
		if (stainName == "") {
			displayName = displayName + ": " + stainName;
		}
		if (compensated == true) {
			displayName = "[" + displayName + "]";
		}
		return displayName;
	}

	public static double[] getMaskColumn(boolean[] mask, double[] column) {
		int newSize = 0;
		for (final boolean value : mask) {
			if (value == true) {
				newSize++;
			}
		}
		final double[] maskedColumn = new double[newSize];
		int j = 0;
		for (int i = 0; i < mask.length; i++) {
			if (mask[i] == true) {
				maskedColumn[j] = column[i];
				j++;
			}
		}
		return maskedColumn;
	}

	public static String[] parseParameterList(Hashtable<String, String> keywords) {
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

	public static Boolean validateHeader(Hashtable<String, String> keywords) {
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



	public static Histogram2D create2dHistogram(double[] transformedDomainData, double[] rangeData, int xbins,
			int ybins) {	
		
		return null;
	}
}
