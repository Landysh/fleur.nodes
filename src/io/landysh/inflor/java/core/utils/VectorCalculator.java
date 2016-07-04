package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

public class VectorCalculator {
	
	private double[] data;
	
	private double min;
	private double max;
	public VectorCalculator(double[] data) {
		this.data = data;
		Arrays.parallelSort(this.data);
		this.min = this.data[0];
		this.max = this.data[this.data.length-1]; 	
	}
	
	public double 	getMax()		{return this.max;}
	public double   getMin()		{return this.min;}
	public double[] getSortedData() {return this.data;}
}
