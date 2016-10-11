package io.landysh.inflor.java.core.utils;

import java.text.NumberFormat;
import java.util.BitSet;

public class BitSetUtils {

	public static String frequencyOfParent(BitSet set, int maxDecimalDigits) {
		double num = set.cardinality();
		double denom = set.size();
		double result = num/denom;
		NumberFormat formatter = NumberFormat.getPercentInstance();
		formatter.setMaximumFractionDigits(maxDecimalDigits);
		return formatter.format(result);
	}

}
