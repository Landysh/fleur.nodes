package io.landysh.inflor.java.core.utils;

import io.landysh.inflor.java.core.plots.TransformType;

public abstract class AbstractDisplayTransform {

	static final int bins = 256;
	
	public final TransformType type;
	
	public AbstractDisplayTransform(final TransformType type) {
		this.type = type;
	}
	
	abstract public double[] transform(double[] rawData);

	public TransformType getType() {
		return this.type;
	}
}
