package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.UUID;

public class PlotSpec implements DomainObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4083719531521659606L;
	
	private      String parent;
	private 	 String plotType;
	private 	 String horizontalAxis;
	private 	 String verticalAxis;
	public final String uuid;
	private 	 String	displayName;
	
	/**
	 * @param priorUUID an id previously generated (eg if the object was previously serialized). Set to null to create a new UUID.
	 */
	public PlotSpec (String priorUUID){
	    //Create new UUID if needed. 
		if (priorUUID==null){
			uuid = UUID.randomUUID().toString();
	    } else {
	    	uuid = priorUUID;
	    }
		
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setPlotType(String newValue) {
		this.plotType = newValue;
	}

	public void setHorizontalAxis(String newValue) {
		this.horizontalAxis = newValue;
		
	}
	public void setVerticalAxis(String newValue) {
		this.verticalAxis = newValue;
		
	}

	public String saveToString() {
		// TODO figure out how to save this to a nice string.
		return null;
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public String getPrefferedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadFromString(String objectString) {
		// TODO Auto-generated method stub
	}
}
//EOF