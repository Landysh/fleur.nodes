package io.landysh.inflor.java.core.transforms;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.stanford.facs.logicle.FastLogicle;

public class LogicleTransform extends AbstractDisplayTransform {
	
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
		this.setT(t);
		this.setW(w);
		this.setM(m);
		this.setA(a);
	}

	public double getT() {
		return t;
	}

	public void setT(double t) {
		this.t = t;
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getM() {
		return m;
	}

	public void setM(double m) {
		this.m = m;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
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

	public double transform(double value) {
		return transformCalculator.scale(value);
	}
	public double inverse(double value) {
		return transformCalculator.inverse(value);
	}
	
}
