package io.landysh.inflor.java.core.transforms;

public class BoundDisplayTransform extends AbstractDisplayTransform {
	
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
		double[] transformedData = rawData.clone();
		for (double d: transformedData){
			if (d<boundaryMin){
				d = boundaryMin;
			}else if (d>boundaryMax){
				d = boundaryMax;
			} else {
				
			}
		}
		return rawData;
	}

	public double getMinValue() {
		return boundaryMin;
	}
	
	public double getMaxValue() {
		return boundaryMax;
	}
}