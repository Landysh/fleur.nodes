package io.landysh.inflor.java.core.transforms;

public class BoundDisplayTransform extends AbstractTransform{
	
	/**
	 * Version history:
	 * v0.1 - 9121057400595419486L 
	 * initial ID.
	 * 
	 */
	private static final long serialVersionUID = 9121057400595419486L;

	private static final TransformType TYPE = TransformType.Bounded;
	
	private double boundaryMin;
	private double boundaryMax;
	
	/**
	 * 
	 * @param min - this minimum value to be shown on the plot.
	 * @param max - this maximum value visible on the displayed plot.
	 * @param roundOutliers - Whether or not to round outliers. Important for gatiingML and (literal) edge cases.
	 */
	
	public BoundDisplayTransform (double min, double max){
		super(TYPE);
		this.boundaryMin = min;
		this.boundaryMax = max;
	}
	
	@Override
	public double[] transform(double[] rawData) {
		double[] transformedData = new double[rawData.length];
		for (int i=0;i<rawData.length;i++){
			transformedData[i] = transform(rawData[i]);
		}
		return transformedData;
	}

	public double getMinValue() {
		return boundaryMin;
	}
	
	public double getMaxValue() {
		return boundaryMax;
	}

	@Override
	public double transform(double d) {
		if (d<boundaryMin){
			d = boundaryMin;
		} else if (d>boundaryMax){
			d = boundaryMax;
		} else {
			//noop
		}
		return d;
	}

	@Override
	public double inverse(double value) {
		// TODO Auto-generated method stub
		return 0;
	}
}