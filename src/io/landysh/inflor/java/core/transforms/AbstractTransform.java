package io.landysh.inflor.java.core.transforms;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractTransform implements Serializable, Cloneable {

	static final int bins = 256;
	
	public final TransformType type;
	
	public AbstractTransform(final TransformType type) {
		this.type = type;
	}
	
	abstract public double[] transform(double[] rawData);
	abstract public double transform(double value);
	abstract public double inverse(double value);
	abstract public double getMinValue();
	abstract public double getMaxValue();
	
	public TransformType getType() {
		return this.type;
	}
}
