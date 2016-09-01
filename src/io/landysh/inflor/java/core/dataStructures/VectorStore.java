package io.landysh.inflor.java.core.dataStructures;

import java.util.Hashtable;
import java.util.concurrent.ExecutionException;

//UnderConstruction, do not use.
public interface VectorStore {
	Object deserialize(byte[] bytes);

	void finalize() throws ExecutionException;

	String getChecksum() throws ExecutionException;

	double[] getData() throws ExecutionException;

	String getName() throws ExecutionException;

	String getUUID() throws ExecutionException;

	byte[] serialize();

	void setData(Double[] data);

	void setKeywords(Hashtable<String, String> keywords);

	void setName(String s);
}