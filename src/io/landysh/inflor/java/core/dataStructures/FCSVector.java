package io.landysh.inflor.java.core.dataStructures;

import java.util.Hashtable;
import java.util.UUID;

public class FCSVector {

	private final String parameterName;
	private String stainName;
	private int parameterindex;
	private final String uuid;
	private Hashtable<FCSVectorType, double[]> data;
	private boolean isCompensated;
	private Hashtable<String, String> keywords;
	private double displayRangeMin;
	private double displayRangeMax;

	public FCSVector(String name) {
		parameterName = name;
		uuid = UUID.randomUUID().toString();
		data = new Hashtable<FCSVectorType, double[]>();
		keywords = new Hashtable<String, String>();
	}

	public FCSVector(String name, double[] data) {
		parameterName = name;
		this.data.put(FCSVectorType.RAW, data);
		uuid = UUID.randomUUID().toString();
	}

	public double[] getData() {
		double[] array;
		try {
			array = data.get(FCSVectorType.COMP);
		} catch (final NullPointerException e) {
			array = data.get(FCSVectorType.RAW);
		}
		return array;
	}

	public double[] getData(FCSVectorType type) {
		return data.get(type);
	}

	public double getDisplayRangeMax() {
		return displayRangeMax;
	}

	public double getDisplayRangeMin() {
		return displayRangeMin;
	}

	public String getKeyword(String name) {
		return keywords.get(name);
	}

	public String getName() {
		String name;
		if (isCompensated == true && stainName != null) {
			name = "comp_" + parameterName + stainName;
		} else if (isCompensated == true && stainName == null) {
			name = "comp_" + parameterName;
		} else if (isCompensated == false && stainName != null) {
			name = parameterName + stainName;
		} else {
			name = parameterName;
		}
		return name;
	}

	public int getParameterindex() {
		return parameterindex;
	}

	public int getSize() {
		return data.get(FCSVectorType.RAW).length;
	}

	public String getUUID() {
		return uuid;
	}

	public void setCompValue(int i, double d) {
		data.get(FCSVectorType.COMP)[i] = d;
	}

	public void setData(double[] newData, FCSVectorType type) {
		data.put(type, newData);
	}

	public void setDisplayRangeMax(double displayRangeMax) {
		this.displayRangeMax = displayRangeMax;
	}

	public void setDisplayRangeMin(double displayRangeMin) {
		this.displayRangeMin = displayRangeMin;
	}

	public void setRawValue(int i, double d) {
		data.get(FCSVectorType.RAW)[i] = d;
	}

	public void setSize(int rowCount) {
		data.put(FCSVectorType.RAW, new double[rowCount]);
	}

}
