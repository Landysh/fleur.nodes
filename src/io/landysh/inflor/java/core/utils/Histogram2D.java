package io.landysh.inflor.java.core.utils;

public class Histogram2D {
	
	double [][] mask;
	double [][] histogram;
	private double xBinWidth;
	private double yBinWidth;
	private double[] zValues;
	private double[] yBins;
	private double[] xBins;
	private double maxHistogramHeight;
	
	public double getMaxHistogramHeight() {
		return maxHistogramHeight;
	}

	public Histogram2D (double[] xData, double xMin, double xMax, int xBinCount, 
			double[] yData, double yMin, double yMax, int yBinCount){
		
		//histogram contains resulting XYZ dataset
 		xBins = new double[xBinCount*yBinCount];
		yBins = new double[xBinCount*yBinCount];
		zValues = new double[xBinCount*yBinCount];
		
		//Mask is the masked raw data to be used for gating.
		mask = new double[2][xData.length];
		xBinWidth = (xMax-xMin)/xBinCount;
		yBinWidth = (yMax-yMin)/yBinCount;
		
		initializeHistogram(xBinCount, xBinWidth, xMin,  yBinCount, yBinWidth, yMin);
		populateHistogram(xBins, yBins, zValues, xData, yData, xBinWidth, yBinWidth, yBinCount);
		
	}

	public double[] populateHistogram(double[] xBins, double[] yBins, double[] zValues, double[] xData, double[] yData, double xBinWidth, double yBinWidth, int yBinCount) {
		for (int i=0;i<xData.length;i++){
			maxHistogramHeight = Double.MIN_VALUE;
			double x = xData[i];
			double y = yData[i];
			//TODO Questionable...
			int xBin = (int)((double) x/xBinWidth);
			int yBin = (int)((double) y/yBinWidth);
			int histogramIndex = yBinCount*xBin + yBin;
			if (histogramIndex < 0){
				histogramIndex = 0;
			} else if (histogramIndex > zValues.length-1){
				histogramIndex = zValues.length-1;
			} 
			zValues[histogramIndex]++;
			mask[0][i] = (double) xBin * xBinWidth;
			if (zValues[histogramIndex]>maxHistogramHeight){
				maxHistogramHeight = zValues[histogramIndex];
			}
		}
		return zValues;
	}

	public void initializeHistogram(int xBinCount, double xBinWidth, double xMin, 
			int yBinCount, double yBinWidth, double yMin) {
		for (int i=0;i<xBinCount;i++){
			double xBinLeftEdge = i*xBinWidth + xMin; 
			for (int j =0 ;j<yBinCount;j++){
				double yBinLeftEdge = j*yBinWidth + yMin; 
				int currentRowIndex = i*yBinCount+j;
				xBins[currentRowIndex] = xBinLeftEdge;
				yBins[currentRowIndex] = yBinLeftEdge;
				zValues[currentRowIndex] = 0;					
			}
		}
	}

	public double getYBinWidth() {
		return yBinWidth;
	}

	public double getXBinWidth() {
		return xBinWidth;
	}

	public double[] getXBins() {
		return xBins;
	}

	public double[] getYBins() {
		return yBins;
	}

	public double[] getZValues() {
		return zValues;
	}
}