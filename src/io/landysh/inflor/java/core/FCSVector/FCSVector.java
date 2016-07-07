package io.landysh.inflor.java.core.FCSVector;

import java.util.Hashtable;
import java.util.UUID;

public class FCSVector {
	
	private String   								parameterName;
	private String   								stainName;
	private int		 								parameterindex;
	private String 	 								uuid;
	private Hashtable <FCSVectorType, double[]>		data;
	private boolean  								isCompensated;
	private Hashtable <String, String>  			keywords;
	private double 									displayRangeMin;
	private double									displayRangeMax;
	
	
	public FCSVector(String name){
		parameterName = name;
		uuid = UUID.randomUUID().toString();
	}
	
	public String getUUID(){
		return this.uuid;
	}
	
	public String getKeyword(String name){
		return this.keywords.get(name);
	}
	
	public FCSVector(String name, double[] data){
		parameterName = name;
		this.data.put(FCSVectorType.RAW, data);
		uuid = UUID.randomUUID().toString();
	}
	
	public void setData(double[] newData, FCSVectorType type){
		data.put(type,newData);
	}
	
	public String getName() {
		String name;
		if (isCompensated==true&&stainName!=null){
			name = "comp_" + parameterName + stainName; 
		} else if (isCompensated==true && stainName == null){
			name = "comp_" + parameterName;
		} else if (isCompensated==false && stainName!= null){
			name = parameterName + stainName;
		} else {
			name = parameterName;
		}
		return name;
	}
	
	public double[] getData(FCSVectorType type) {
		return data.get(type);
	}

	public void setValue(int i, double d) {
		this.data.get(FCSVectorType.RAW)[i] = d;
	}

	public void setSize(int rowCount) {
		this.data.put(FCSVectorType.RAW, new double[rowCount]);
	}
	
	public int getSize() {
		return this.data.get(FCSVectorType.RAW).length;
	}

	public int getParameterindex() {
		return parameterindex;
	}

	public double getDisplayRangeMax() 						{return displayRangeMax;}	
	public double getDisplayRangeMin() 						{return displayRangeMin;}
	public void setDisplayRangeMax(double displayRangeMax) 	{this.displayRangeMax = displayRangeMax;}
	public void setDisplayRangeMin(double displayRangeMin) 	{this.displayRangeMin = displayRangeMin;}

	
	public double[] getData() {
		double[] array;
		try {
			array = data.get(FCSVectorType.COMP);
		} catch (NullPointerException e){
			array = data.get(FCSVectorType.RAW);
		}
		return array;
	}
}
