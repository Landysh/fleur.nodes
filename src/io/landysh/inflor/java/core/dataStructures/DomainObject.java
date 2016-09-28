package io.landysh.inflor.java.core.dataStructures;

import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("serial")
public abstract class DomainObject implements Serializable{
	

	/**
	 * An abstract class used to create persistent and identifiable objects. Responsible for the creation of UUIDs and SERDE
	 * @Param priorUUID - a previously generated ID. Use null to create a new ID if a suitable one is not available. 
	 */
	
	public final String ID;
	
	private String displayName;

	public DomainObject() {
		ID = UUID.randomUUID().toString();
	}
	public String getDisplayName(){
		if (displayName!=null){
			return displayName;
		} else {
			return ID;
		}
	}
	public void setDisplayName(String newName){
		this.displayName = newName;
	}
}
