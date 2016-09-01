package io.landysh.inflor.java.core.regressor;

public interface Regressor {
	public void learn(double t);

	public double[] predict(double[][] x);
}
