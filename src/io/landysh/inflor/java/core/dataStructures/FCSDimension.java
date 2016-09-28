package io.landysh.inflor.java.core.dataStructures;

import java.util.Hashtable;

public class FCSDimension {

	private final String dimensionName;

	private final double[] data;
	private Hashtable<String, String> keywords;

	public FCSDimension(String name, double[] data, Hashtable<String, String> keywords) {
		dimensionName = name;
		this.data = data;
		keywords = new Hashtable<String, String>();
	}

	public double[] getData() {
		return data;
	}

	public String getKeyword(String name) {
		return keywords.get(name);
	}

	public int getSize() {
		return this.data.length;
	}

	public String getName() {
		return dimensionName;
	}
	
}
