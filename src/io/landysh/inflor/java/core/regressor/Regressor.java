package io.landysh.inflor.java.core.regressor;

public interface Regressor {
	public double[] predict(double[][] x);

	public void learn(double t);
}
