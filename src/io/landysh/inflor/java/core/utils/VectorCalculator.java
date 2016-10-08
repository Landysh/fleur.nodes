package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

public class VectorCalculator {

	private final double[] data;
	private final double[] sortedData;

	private final double min;
	private final double max;

	public VectorCalculator(double[] data) {
		this.data = data;
		sortedData = data.clone();
		Arrays.parallelSort(sortedData);
		min = sortedData[0];
		max = sortedData[sortedData.length - 1];
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double[] getRawData() {
		return data;
	}

	public double[] getSortedData() {
		return sortedData;
	}
	
	public double getPercentile(double percentile) {
		//TODO: This is quick and dirty, need a correct implementation. 	
		if (0<=percentile&&percentile<1){
			int percentileIndex = (int)(percentile*sortedData.length);
			return sortedData[percentileIndex];
		} else {
			throw new IllegalArgumentException("The requested percentile must be in the range 0->1");
		}
	}
	
}