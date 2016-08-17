package io.landysh.inflor.java.core.gatingML.gates;

import java.util.UUID;

import org.w3c.dom.Document;

public class GatingStrategy {
	
	String uuid;
	
	public GatingStrategy(boolean createNewUUID){
	    //Create new UUID if needed.  Otherwise, assume we are parsing an existing gml document.
		if (createNewUUID){
	    	uuid = UUID.randomUUID().toString();
	    }
	}
	
	public String getId() {
		return uuid;
	}
	
	public void setUUID(String uuid){
		this.uuid = uuid;	
	}

	public Document toGatingML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static GatingStrategy load(String gmlString){
		GatingStrategy gs = new GatingStrategy(false);
		//TODO:
		return gs;
	}

}
