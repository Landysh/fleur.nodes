package io.landysh.inflor.java.core.sne;

import java.util.Hashtable;

public class BarnesHutTSNE {
	
	private double[][] X;
	private double[][] Y;
	
	private int maxIter 	= 400;
	private int currentIter = 0;
	private int perplexity 	= 40;
	
	private double[][] currentData;	
	private Hashtable<Integer, double[][]> interactiveLog;
	private int interactiveRunIteration=0;
	
	
	public BarnesHutTSNE (double[][] X, int maxIters, int perplexity){
		this.X 			= X;
		this.Y 			= new double[2][this.X[0].length];
		this.maxIter 	= maxIters;
		this.perplexity = perplexity;
		//TODO: other model initialization here
	}
	
	public double[][] getCurrentData(){
		return currentData;
	}
	
	public double getProgress(){
		return currentIter/maxIter;
	}
	
	public double[][] run(){
		//TODO Any calculation heavy setup.
		
		//TODO: Iterate
		boolean keepGoing = true;
		while (keepGoing == true){
			advance();
		}
		return this.Y;
	}
	
	private double[][] advance() {
		//TODO: implement each step in the iteration process here.	
		return currentData;
	}

	public double[][] runInteractively(int updateRate){
		interactiveLog = new Hashtable<Integer, double[][]>();
		try {
			currentData = advance();
			return currentData;
		} catch (Exception e){
			throw new RuntimeException("Execution Finished.");
		}
	}
}
//EOF