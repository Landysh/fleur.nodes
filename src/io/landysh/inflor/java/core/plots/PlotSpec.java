package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import io.landysh.inflor.java.core.dataStructures.DomainObject;

public class PlotSpec implements DomainObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4083719531521659606L;

	private String parent;
	private String plotType;
	private String horizontalAxis;
	private String verticalAxis;
	public final String uuid;
	private String displayName;

	/**
	 * @param priorUUID
	 *            an id previously generated (eg if the object was previously
	 *            serialized). Set to null to create a new UUID.
	 */
	public PlotSpec(String priorUUID) {
		// Create new UUID if needed.
		if (priorUUID == null) {
			uuid = UUID.randomUUID().toString();
		} else {
			uuid = priorUUID;
		}

	}

	public String getParent() {
		return parent;
	}

	@Override
	public String getPrefferedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public void loadFromString(String objectString) {
		// TODO Auto-generated method stub
	}

	@Override
	public String saveToString() {
		// TODO figure out how to save this to a nice string.
		return null;
	}

	public void setHorizontalAxis(String newValue) {
		horizontalAxis = newValue;

	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setPlotType(String newValue) {
		plotType = newValue;
	}

	public void setVerticalAxis(String newValue) {
		verticalAxis = newValue;

	}
}
// EOF