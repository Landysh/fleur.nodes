package io.landysh.inflor.java.core.dataStructures;

public class Histogram1D {

	private double[] x;
	private double[] y;

	public Histogram1D(double[] data, double min, double max, int binCount) {
		
	double deltaX = (max-min)/binCount;

	x = new double[binCount];
	for(int i=0;i<binCount;i++){
		x[i] = i*deltaX;
	}
	y = new double[binCount];
	for(int i=0;i<binCount;i++){y[i] = 0;}
	for(int i=0;i<data.length;i++){
		int bin = (int) (data[i]/deltaX);
		if (bin>=y.length){
			i=i;
		}
		y[bin]++;
		}
	}
	
	public double[][] getData(){
		return new double[][] {x,y};
	}
}
