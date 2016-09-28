package io.landysh.inflor.java.core.transforms;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractDisplayTransform implements Serializable{

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
