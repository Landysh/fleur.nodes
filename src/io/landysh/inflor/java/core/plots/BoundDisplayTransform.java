package io.landysh.inflor.java.core.plots;

import io.landysh.inflor.java.core.utils.AbstractDisplayTransform;

public class BoundDisplayTransform extends AbstractDisplayTransform {
	
	private double boundaryMin;
	private double boundaryMax;
	private boolean roundOutliers;
	
	/**
	 * 
	 * @param min - this minimum value to be shown on the plot.
	 * @param max - this maximum value visible on the displayed plot.
	 * @param roundOutliers - Whether or not to round outliers. Important for gatiingML and (literal) edge cases.
	 */
	
	public BoundDisplayTransform (double min, double max, boolean roundOutliers){
		this.boundaryMin = min;
		this.boundaryMax = max;
		this.roundOutliers = roundOutliers;
	}
	
	@Override
	public double[] transform(double[] rawData) {
		double[] transformedData = rawData.clone();
		if (roundOutliers==true){
			for (double d: transformedData){
				if (d<boundaryMin){
					d = boundaryMin;
				}else if (d>boundaryMax){
					d = boundaryMax;
				} else {
					
				}
			}
		}
		return rawData;
	}
}