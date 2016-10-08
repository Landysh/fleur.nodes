package io.landysh.inflor.java.core.singlets;

import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.math3.stat.StatUtils;

public class SingletsModel {

	String[] initialColumns = null;
	ArrayList<String> areaColumnNames = null;
	ArrayList<String> heightColumnNames = null;
	ArrayList<String> widthColumnNames = null;

	double ratioThreshold;

	public SingletsModel(String[] columnNames) {
		initialColumns = columnNames;
		areaColumnNames = findColumns(columnNames, PuleProperties.AREA);
		heightColumnNames = findColumns(columnNames, PuleProperties.HEIGHT);
		widthColumnNames = findColumns(columnNames, PuleProperties.WIDTH);
	}

	public double[] buildModel(double[] area, double[] height) {
		final double[] ratioAH = ratio(area, height);
		// This only really works for homogeneous particles.
		final double minimum = StatUtils.percentile(ratioAH, 1);
		final double median = StatUtils.percentile(ratioAH, 50);
		ratioThreshold = (median - minimum) + median;
		return ratioAH;
	}

	public ArrayList<String> findColumns(String[] columnNames, PuleProperties type) {
		/**
		 * Applies the regular expressions from the PulseProperties Enum.
		 */
		final String[] expressions = type.regi();
		ArrayList<String> foundColumns = new ArrayList<String>();
		for (final String s : columnNames) {
			for (final String regex : expressions) {
				if (s.matches(regex)) {
					foundColumns.add(s);
				}
			}
		}
		if (foundColumns.size() == 0) {
			foundColumns = new ArrayList<String>();
			foundColumns.add("None");
		}
		return foundColumns;
	}

	private double[] ratio(double[] a, double[] b) {
		final double[] ratio = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			ratio[i] = a[i] / b[i];
		}
		return ratio;
	}

	public BitSet scoreModel(double[] ratio) {
		final BitSet mask = new BitSet(ratio.length);
		for (int i = 0; i < mask.size(); i++) {
			if (ratio[i] <= ratioThreshold) {
				mask.set(i);
			}
		}
		return mask;
	}
}
