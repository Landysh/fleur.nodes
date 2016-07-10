package io.landysh.inflor.java.core.regressor;

import java.util.Hashtable;

public class DRRegressor {
	
	private double[][] X;
	private double[][] Y;
	
	Hashtable<String, MLPRegressor> models;
	
	public DRRegressor(double[][] X, double[][] Y ){
		this.X = X;
		this.Y = Y;
	}
	
	public void trainDRRModel(double crossValThreshold){		
		for (double[] target:Y){
			createValidatedModel(X, target, crossValThreshold);
		}	
	}

	private void createValidatedModel(double[][] x2, double[] target, double crossValThreshold) {
		// TODO Auto-generated method stub
	}
}
