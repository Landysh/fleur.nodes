package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.Serializable;
import java.util.UUID;

import io.landysh.inflor.java.core.plots.AbstractFACSPlot;

public class PlotSpec implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4083719531521659606L;
	private String parent;
	private String plotType;
	private String horizontalAxis;
	private String verticalAxis;
	public final String uuid;
	
	/**
	 * 
	 * @param priorUUID an id previously generated (eg if the object was previously serialized). Set to null to create a new UUID.
	 */
	public PlotSpec(String priorUUID){
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

	public static PlotSpec load(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public String saveToString() {
		// TODO figure out how to coerce this to a nice string.
		return null;
	}
}
//EOF