package io.landysh.inflor.java.core;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
//UnderConstruction, do not use.
public interface VectorStore {
	void     setName (String s);
	void     setData (Double[] data);
	void     setKeywords (Hashtable <String, String> keywords);
	void     finalize()    throws ExecutionException;
	String   getChecksum() throws ExecutionException;
	String   getName()     throws ExecutionException;
	String   getUUID()     throws ExecutionException;
	double[] getData()     throws ExecutionException;
	byte[]   serialize();
	Object   deserialize(byte[] bytes);
}