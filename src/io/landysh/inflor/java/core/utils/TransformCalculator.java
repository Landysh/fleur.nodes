package io.landysh.inflor.java.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.stanford.facs.logicle.FastLogicle;

public class TransformCalculator {
	
	//TODO: move this code to AbstractTransforms.
	
	// As suggested by Parks/Moore.
	private static final double LOGICLE_W_PERCENTILE = 0.05;
	private static final double DEFAULT_M = 4.5;
	private static final double DEFAULT_A = 0;

	private FastLogicle logicleTransform;

	public double[] centered(double[] X) {
		final double[] Y = X.clone();
		// TODO
		return Y;
	}

	private double estimateW(double[] X, double T, double M) {
		/**
		 * Based on the 5th percentile method suggested by Parks/Moore.
		 */
		final double lowerBound = new Percentile().evaluate(X, LOGICLE_W_PERCENTILE);
		final double W = (M - Math.log10(T / Math.abs(lowerBound))) / 2;
		return W;
	}

	public double[] linear(double[] X, double m, double b) {
		final double[] Y = X.clone();
		for (double d : Y) {
			d = m * d + b;
		}
		return X;
	}

	public double[] log(double[] X) {
		final double[] Y = X.clone();
		for (double d : Y) {
			d = Math.log10(d);
		}
		return Y;
	}

	public double[] logicle(double[] X) {
		final double t = StatUtils.max(X);
		final double[] Y = logicle(X, t);
		return Y;
	}

	public double[] logicle(double[] X, double T) {
		final double W = estimateW(X, T, DEFAULT_M);
		final double[] Y = logicle(X, T, W);
		return Y;
	}

	public double[] logicle(double[] X, double T, double W) {
		final double[] Y = logicle(X, T, W, DEFAULT_M);
		return Y;
	}

	public double[] logicle(double[] X, double T, double M, boolean b) {
		/**
		 * Use to calculate W with a custom M. Boolean argument ignored but
		 * needed to differentiate from 'double[] logicle (double[] X, double T,
		 * double W){...}'
		 */
		final double W = estimateW(X, T, M);
		final double[] Y = logicle(X, T, W, M);
		return Y;
	}

	public double[] logicle(double[] X, double T, double W, double M) {
		final double[] Y = logicle(X, T, W, M, DEFAULT_A);
		return Y;
	}

	public double[] logicle(double[] X, double T, double W, double M, double A) {
		final double[] Y = X.clone();
		logicleTransform = new FastLogicle(T, W, M, A);
		for (double d : Y) {
			d = logicleTransform.scale(d);
		}
		return Y;
	}

	public double[] logicleInverse(double[] X) {
		final double[] Y = X.clone();
		for (double d : Y) {
			d = logicleTransform.inverse(d);
		}
		return Y;
	}

	public double[] logStream(double[] X) {
		final double[] Y = X.clone();
		final MathLogConsumer doLog10 = new MathLogConsumer();
		Arrays.stream(Y).forEach(doLog10);
		return Y;
	}
}
