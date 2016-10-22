package io.landysh.inflor.java.core.transforms;

public class LogrithmicTransform extends AbstractTransform {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5678244382085424064L;

	private static final TransformType TYPE = TransformType.Logrithmic;
	
	private double min;
	private double max;

	
	public LogrithmicTransform(double min, double max) {
		super(TYPE);
		this.min = min;
		this.max = max;
	}

	@Override
	public double[] transform(double[] rawData) {
		double[] transformedData = new double[rawData.length];
		for (int i=0;i<rawData.length;i++){
			transformedData[i] = transform(rawData[i]);
		}
		return transformedData;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public double transform(double value) {
		if (value < min){value = min;}
		return Math.log10(value);
	}

	@Override
	public double inverse(double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinValue() {
		return Math.log(min);
	}

	@Override
	public double getMaxValue() {
		return Math.log(max);
	}
}
