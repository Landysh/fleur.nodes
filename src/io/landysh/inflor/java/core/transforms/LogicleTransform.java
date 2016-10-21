package io.landysh.inflor.java.core.transforms;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.stanford.facs.logicle.FastLogicle;

@SuppressWarnings("serial")
public class LogicleTransform extends AbstractTransform {
	
	private static final TransformType TYPE = TransformType.Logicle;
	private static final double LOGICLE_W_PERCENTILE = 0.05;
	private double t = 262144;
	private double w = 0.5;
	private double m = 4.5;
	private double a = 0;
	FastLogicle transformCalculator;
	
	public LogicleTransform(int binCount) {
		super(TYPE);
		this.transformCalculator = new FastLogicle(t, w, m, a, binCount);
	}

	@Override
	public double[] transform(double[] rawData) {
		double[] newData = new double[rawData.length]; 
		for (int i=0;i<rawData.length;i++){
			newData[i] = transformCalculator.scale(rawData[i]);
		}
		return newData;
	}
	
	public double[] inverse(double[] transformedData) {
		double[] newData = new double[transformedData.length]; 
		for (int i=0;i<transformedData.length;i++){
			newData[i] = transformCalculator.inverse(transformedData[i]);
		}
		return newData;
	}
	
	public void setParameters(double t, double w, double m, double a){
		this.t =t;
		this.w=w;
		this.m=m;
		this.a=a;
	}

	public double calculateW(double[] data){
		/**
		 * Based on the 5th percentile method suggested by Parks/Moore.
		 */
		final double lowerBound = new Percentile().evaluate(data, LOGICLE_W_PERCENTILE);
		final double newWidth = (m - Math.log10(t / Math.abs(lowerBound))) / 2;
		this.w = newWidth;
		return this.w;
	}
	@Override
	public double transform(double value) {
		return transformCalculator.scale(value);
	}
	@Override
	public double inverse(double value) {
		return transformCalculator.inverse(value);
	}

	public double getMinValue() {
		return transform(-100);
	}

	public double getMaxValue() {
		return transform(t-1);
	}	
}
