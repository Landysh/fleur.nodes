package io.landysh.inflor.java.core.transforms;

public class LogrithmicDisplayTransform extends AbstractDisplayTransform {
	
	private static final TransformType TYPE = TransformType.Logrithmic;
	
	private double min;
	private double max;

	
	public LogrithmicDisplayTransform(double min, double max) {
		super(TYPE);
		this.min = min;
		this.max = max;
	}

	@Override
	public double[] transform(double[] rawData) {
		return null;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}
