package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.utils.AbstractDisplayTransform;

public class PlotSpec implements DomainObject {
	private static final int DEFAULT_BIN_COUNT = 256;
	private static final int DEFAULT_SCALE_MINIMUM = 0;
	private static final int DEFAULT_SCALE_MAXIMUM = 262144;

	public final String uuid;

	private String      	parent;
	private PlotTypes   	plotType;
	private String      	domainAxisName;
	private String      	rangeAxisName;
	
	private AbstractDisplayTransform domainTransform;
	private AbstractDisplayTransform rangeTransform;


	public int getyBinCount() {
		return yBinCount;
	}

	public void setyBinCount(int yBinCount) {
		this.yBinCount = yBinCount;
	}

	public int getxBinCount() {
		return xBinCount;
	}

	public void setxBinCount(int xBinCount) {
		this.xBinCount = xBinCount;
	}

	public double getxMin() {
		return xMin;
	}

	public void setxMin(double xMin) {
		this.xMin = xMin;
	}

	public double getyMin() {
		return yMin;
	}

	public void setyMin(double yMin) {
		this.yMin = yMin;
	}

	public double getyMax() {
		return yMax;
	}

	public void setyMax(double yMax) {
		this.yMax = yMax;
	}

	public double getxMax() {
		return xMax;
	}

	public void setxMax(double xMax) {
		this.xMax = xMax;
	}

	private int    yBinCount = DEFAULT_BIN_COUNT;
	private int    xBinCount = DEFAULT_BIN_COUNT;
	private double xMin = DEFAULT_SCALE_MINIMUM;
	private double yMin = DEFAULT_SCALE_MINIMUM;
	private double yMax = DEFAULT_SCALE_MAXIMUM;
	private double xMax	= DEFAULT_SCALE_MAXIMUM;


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

	public PlotSpec() {
		this(null);
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

	public void setVerticalAxis(String name) {
		this.rangeAxisName = name;

	}

	public PlotTypes getPlotType() {
		return plotType;
	}

	public String getDomainAxisName() {
		return domainAxisName;
	}

	public String getRangeAxisName() {
		return rangeAxisName;
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

	public void setDomainTransform(BoundDisplayTransform newTransform) {
		this.domainTransform = newTransform;
	}

	public double getXMin() {
		return xMin;
	}

	public double getYMin() {
		return yMin;
	}

	public int getXBinCount() {
		return xBinCount;
	}

	public double getXMax() {
		return xMax;
	}

	public double getYMax() {
		return yMax;
	}

	public int getYBinCount() {
		return yBinCount;
	}

}
// EOF