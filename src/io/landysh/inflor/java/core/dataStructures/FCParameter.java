package io.landysh.inflor.java.core.dataStructures;

import java.util.Hashtable;

public class FCParameter {

	private final String parameterName;
	private final int size;

	private Hashtable<FCVectorType, double[]> data;
	private Hashtable<String, String> keywords;

	public FCParameter(String name, final int size) {
		parameterName = name;
		this.size = size;
		data = new Hashtable<FCVectorType, double[]>();
		keywords = new Hashtable<String, String>();
	}
	//Interface method?
	public double[] getData() {
		double[] array;
		try {
			array = data.get(FCVectorType.COMP);
		} catch (final NullPointerException e) {
			array = data.get(FCVectorType.RAW);
		}
		return array;
	}

	public double[] getData(FCVectorType type) {
		if (data.get(type)==null){
			double[] newData = new double[this.size];
			this.data.put(type, newData);
		}
		return data.get(type);
	}

	public String getKeyword(String name) {
		return keywords.get(name);
	}

	public int getSize() {
		return data.get(FCVectorType.RAW).length;
	}

	public void setData(double[] newData, FCVectorType type) {
		data.put(type, newData);
	}
	public String getName() {
		return parameterName;
	}
	
	public String getName(FCVectorType type) {
		//TODO
		return parameterName;
	}
	
}
