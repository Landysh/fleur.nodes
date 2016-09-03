package io.landysh.inflor.java.core.utils;

public class Histogram2D {
	int[][] mask;
	double[][] histogram;
	
	public Histogram2D (double[] xData, double xMin, double xMax, int xBins, double[] yData, double yMin, double yMax, int yBins){
		//histogram contains resulting XYZ dataset
		histogram = new double[3][xBins*yBins];
		//Mask is the masked raw data to be used for gating.
		mask = new int[2][xData.length];
		double xBinWidth = (xMax-xMin)/xBins;
		double yBinWidth = (yMax-yMin)/yBins;
		for (int i=0;i<xData.length;i++){
			
		}
	}
}
