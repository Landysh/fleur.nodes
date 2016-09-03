package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.utils.AbstractDisplayTransform;

public class PlotSpec implements DomainObject {
	public final String uuid;

	
	private String      	parent;
	private PlotTypes   	plotType;
	private String      	domainAxisName;
	private String      	rangeAxisName;


	private AbstractDisplayTransform domainTransform;
	private AbstractDisplayTransform rangeTransform;

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
		domainAxisName = newValue;

	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setPlotType(PlotTypes newValue) {
		plotType = newValue;
	}

	public void setVerticalAxis(String newValue) {
		setRangeAxisName(newValue);

	}

	public PlotTypes getPlotType() {
		return plotType;
	}

	public String getDomainName() {
		return domainAxisName;
	}

	public String getRangeAxisName() {
		return rangeAxisName;
	}

	public void setRangeAxisName(String rangeAxisName) {
		this.rangeAxisName = rangeAxisName;
	}

	public AbstractDisplayTransform getDomainTransform() {
		return this.domainTransform;
	}

	public AbstractDisplayTransform getRangeTransform() {
		return rangeTransform;
	}

	public void setRangeTransform(AbstractDisplayTransform rangeTransform) {
		this.rangeTransform = rangeTransform;
	}
}
// EOF