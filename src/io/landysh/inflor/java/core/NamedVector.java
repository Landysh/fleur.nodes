package io.landysh.inflor.java.core;

import java.util.Hashtable;

public interface NamedVector {
	String   					getName();
	double[] 					getData();
	Hashtable<String, String> 	getKeywords();
	byte[] 						save();
	NamedVector					load(byte[] bytes);
	void 						setKeywords(Hashtable <String, String> keywords);
}
