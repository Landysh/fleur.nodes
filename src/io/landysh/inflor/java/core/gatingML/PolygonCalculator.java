package io.landysh.inflor.java.core.gatingML;

import java.util.ArrayList;

public class PolygonCalculator {
	
	double[] d1;
	double[] d2;
	
	public PolygonCalculator(ArrayList<Double> D1, ArrayList<Double> D2) {
		if (D1.size()!=D2.size()){
			String message = "d1 and d2 must have the name size";
			IllegalArgumentException e = new IllegalArgumentException(message);
			e.printStackTrace();
			throw e;
		}	
		d1 = new double[D1.size()]; 
		d2 = new double[D1.size()]; 
		for (int i=0;i<D1.size();i++){
			d1[i] = D1.get(i);
			d2[i] = D2.get(i);
		}
	}
	
	public boolean isInside(double d1Test, double d2Test){
		//TODO - Winding number method?
		return false;
	}
}
