package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

import edu.stanford.facs.logicle.FastLogicle;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.StatUtils;


public class TransformCalculator {	
	
	// As suggested by Parks/Moore.
	private static final double LOGICLE_W_PERCENTILE = 0.05;
	private static final double DEFAULT_M = 4.5;
	private static final double DEFAULT_A = 0;

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
	
	public double[] logicle (double[] X, double T, double W, double M, double A){
		double[] Y =X.clone();
		this.logicleTransform = new FastLogicle(T,W,M,A);
		for (double d:Y){d = logicleTransform.scale(d);}
		return Y;
	}
	
	public double[] logicle (double[] X, double T, double W, double M){
		double[] Y = logicle(X, T, W, M, DEFAULT_A);
		return Y;
	}
	
	public double[] logicle (double[] X, double T, double W){
		double[] Y = logicle(X, T, W, DEFAULT_M);
		return Y;
	}
	
	public double[] logicle (double[] X, double T){
		double W = estimateW(X, T, DEFAULT_M);
		double[] Y = logicle(X, T, W);
		return Y;
	}
	public double[] logicle (double[] X, double T, double M, boolean b){
		/**
		 * Use to calculate W with a custom M.  Boolean argument ignored but needed
		 * to differentiate from 'double[] logicle (double[] X, double T, double W){...}'
		 */
		double W = estimateW(X, T, M);
		double[] Y = logicle(X, T, W, M);
		return Y;
	}
	
	
	public double[] logicle (double[] X){
		double t = StatUtils.max(X);
		double[] Y = logicle(X, t);
		return Y;
	}
	
	private double estimateW(double[] X, double T, double M) {
		/**
		 * Based on the 5th percentile method suggested by Parks/Moore.
		 */
		double lowerBound = new Percentile().evaluate(X, LOGICLE_W_PERCENTILE);
		double W = (M-Math.log10(T/Math.abs(lowerBound)))/2;
		return W;
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
