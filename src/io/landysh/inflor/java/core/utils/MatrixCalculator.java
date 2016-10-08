package io.landysh.inflor.java.core.utils;

public class MatrixCalculator {

	public static double[][] pow(double[][] X, int p) {
		for (int i = 0; i < X.length; i++) {
			for (int j = 0; j < X[0].length; j++) {
				X[i][j] = Math.pow(X[i][j], p);
			}
		}
		return X;
	}
	
	public static double[][] transpose(double[][] X) {
		double[][] XT = new double[X[0].length][X.length];
		for (int i=0; i<X.length; i++) {
			for (int j=0; j<X[0].length; j++) {
				XT[j][i] = X[i][j];
			}
		}
		return XT;
	}

}
