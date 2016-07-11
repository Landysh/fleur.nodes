package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

import edu.stanford.facs.logicle.FastLogicle;

public class TransformCalculator {	
	
	private FastLogicle logicleTransform;

	public double[] linear (double[] X, double m, double b){
		double[] Y = X.clone();
		for (double d:Y){d = m*d+b;}
		return X;
	}
	
	public double[] logStream (double[] X){
		double[] Y = X.clone();
		MathLogConsumer doLog10 = new MathLogConsumer();
		Arrays.stream(Y).forEach(doLog10);
		return Y;
	}
	
	public double[] log (double[] X){
		double[] Y = X.clone();
		for (double d:Y){d = Math.log10(d);}
		return Y;
	}
	
	public double[] logicle (double[] X, double t, double w, double m, double a){
		double[] Y =X.clone();
		this.logicleTransform = new FastLogicle(t,w,m,a);
		for (double d:Y){d = logicleTransform.scale(d);}
		return Y;
	}
	
	public double[] logicleInverse (double[] X){
		double[] Y = X.clone();
		for (double d:Y){d = logicleTransform.inverse(d);}
		return Y;
	}
	
	public double[] centered(double[] X){
		double[] Y=X.clone();
		//TODO
		return Y;
	}
}
