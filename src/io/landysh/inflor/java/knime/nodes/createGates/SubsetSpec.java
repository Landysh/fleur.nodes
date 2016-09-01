package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.UUID;

public class SubsetSpec implements DomainObject{
	String uuid;
	String displayName;
	
	public SubsetSpec(String priorUUID){
	    //Create new UUID if needed. 
		if (priorUUID==null){
			uuid = UUID.randomUUID().toString();
	    } else {
	    	uuid = priorUUID;
	    }
	}

	@Override
	public String saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public String getPrefferedName() {
		if (displayName == null){
			return uuid;
		} else {
			return displayName;
		}
	}

	@Override
	public void loadFromString(String objectString) {
		// TODO Auto-generated method stub
	}
}
