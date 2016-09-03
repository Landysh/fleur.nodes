package io.landysh.inflor.java.core.utils;

public abstract class AbstractDisplayTransform {

	static final int bins = 512;
	
	abstract public double[] transform(double[] rawData);
}
