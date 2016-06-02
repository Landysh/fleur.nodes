package io.landysh.inflor.java.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.UUID;

public class FCSVector implements NamedVector {
	
	private String   					parameterName;
	private String   					stainName;
	private int		 					parameterindex;
	private String   					checksum;
	private String 	 					uuid;
	private double[] 					data;
	private byte[]   					bytes;
	private boolean  					isCompensated;
	private Hashtable <String, String>  keywords;
	
	public FCSVector(String name){
		parameterName = name;
		uuid = UUID.randomUUID().toString();
	}
	
	public FCSVector(){
		//Empty constructor to be used with .load(bytes)
	}
	
	private String updateChecksum (byte[] inBytes){
		
		StringBuffer buffer = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(inBytes);
			
		    byte[] SHABytes = messageDigest.digest();
		    buffer = new StringBuffer();
		    for (int i = 0; i < bytes.length; i++) {
		     	buffer.append(Integer.toString((SHABytes[i] & 0xff) + 0x100, 16).substring(1));
		    }        
		} catch (NoSuchAlgorithmException e) {
			// Should never happen in this context. Algorithm is hard coded.
			e.printStackTrace();
		}
		return buffer.toString();
	}
	public void save(FileOutputStream out) throws IOException {
			byte[] bytes = this.getBytes();
			out.write(bytes);
			out.flush();
			
	}
	private byte[] getBytes() {
		// TODO
		return null;
	}
	public void setData(double[] newData){
		data = newData;
		bytes = getBytes();
		checksum = updateChecksum(bytes);
	}
	@Override
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
	
	@Override
	public double[] getData() {
		return data;
	}
	@Override
	public byte[] save() {
		return bytes;
	}
	@Override
	public FCSVector load(byte[] bytes) {
		// TODO eventually write each column to a separate file.
	return null;
	}
	@Override
	public Hashtable<String, String> getKeywords() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setKeywords(Hashtable<String, String> keywords) {
		// TODO Auto-generated method stub
		
	}
}
