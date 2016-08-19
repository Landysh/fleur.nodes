package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

public class VectorCalculator {

	private double[] data;
	private double[] sortedData;

	private double min;
	private double max;

	public VectorCalculator(double[] data) {
		this.data = data;
		this.sortedData = data.clone();
		Arrays.parallelSort(this.sortedData);
		this.min = this.sortedData[0];
		this.max = this.sortedData[this.sortedData.length - 1];
	}

	public double getMax() {
		return this.max;
	}

	public double getMin() {
		return this.min;
	}

	public double[] getSortedData() {
		return this.sortedData;
	}
	
	public double[] getRawData(){
		return this.data;
	}
}