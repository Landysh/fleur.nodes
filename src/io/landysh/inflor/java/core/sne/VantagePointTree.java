package io.landysh.inflor.java.core.sne;

public class VantagePointTree {

	private double[][] data;

	public VantagePointTree(double[][] newData) {
		this.data = newData;
	}
	
	public double[][] getData(){
		return this.data;
	}
}
