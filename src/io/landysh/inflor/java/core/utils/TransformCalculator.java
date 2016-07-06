package io.landysh.inflor.java.core.utils;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

public class TransformCalculator {

	double[] data;
	
	public TransformCalculator (double[] data){
		this.data = data;
	}
	
	public double[] linear (double m, double b){
		double[] out = this.data.clone();
		for (double d:out){d = m*d+b;}
		return out;
	}
	
	public double[] logStream (int max, int decades){
		double[] out = this.data.clone();
		MathLogConsumer doLog10 = new MathLogConsumer();
		Arrays.stream(out).forEach(doLog10);
		return out;
	}
	
	public double[] log (){
		double[] out = this.data.clone();
		for (double d:out){d = Math.log10(d);}
		return out;
	}
	
	public double[] logicle (int max, int decades){
		double[] out = this.data.clone();
		for (double d:out){d = Math.log10(d);}
		return out;
	}
	
	public double[] centered(){
		double[] out = new double[this.data.length];
		
		return out;
	}
}
