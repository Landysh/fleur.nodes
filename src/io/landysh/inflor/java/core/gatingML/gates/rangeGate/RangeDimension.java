package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGMLDimension;

public class RangeDimension extends AbstractGMLDimension {

	private Double min = Double.MIN_VALUE;
	private Double max = Double.MAX_VALUE;

	public RangeDimension(String name) {
		super(name);
	}

	public boolean evaluate(double value) {
		if (min <= value && value < max) {
			return true;
		} else {
			return false;
		}
	}

	public Double getMax() {
		return max;
	}

	public Double getMin() {
		return min;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public void setMin(Double min) {
		this.min = min;
	}
}
// EOF