package io.landysh.inflor.java.core.plots;

import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.transforms.AbstractDisplayTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;

public class ChartSpec extends DomainObject {
	
	/**
	 * Version 0.1 of the ChartSpec. Once a 1.0 stable release is made any changes that break backward 
	 * compatibility must increment the serialID
	 */
	private static final long serialVersionUID = 7153659120835974973L;

	private static final int DEFAULT_BIN_COUNT = 256;
	private static final int DEFAULT_SCALE_MINIMUM = 0;
	private static final int DEFAULT_SCALE_MAXIMUM = 262144;
	
	private static final AbstractDisplayTransform DEFAULT_TRANSFORM = 
			new BoundDisplayTransform(DEFAULT_SCALE_MINIMUM, DEFAULT_SCALE_MAXIMUM);
	private static final PlotTypes DEFAULT_CHART_TYPE = PlotTypes.Scatter;

	private String      	parent;
	private PlotTypes   	plotType = DEFAULT_CHART_TYPE;
	private String      	domainAxisName;
	private String      	rangeAxisName;
	private int    			yBinCount = DEFAULT_BIN_COUNT;
	private int    			xBinCount = DEFAULT_BIN_COUNT;
	private double 			xMin 	  = DEFAULT_SCALE_MINIMUM;
	private double 			yMin      =	DEFAULT_SCALE_MINIMUM;
	private double 			yMax 	  = DEFAULT_SCALE_MAXIMUM;
	private double 			xMax	  =	DEFAULT_SCALE_MAXIMUM;

	private AbstractDisplayTransform domainTransform = DEFAULT_TRANSFORM;
	private AbstractDisplayTransform rangeTransform = DEFAULT_TRANSFORM;

	private String displayName;

	/**
	 * @param priorUUID
	 *            an id previously generated (eg if the object was previously
	 *            serialized). Set to null to create a new UUID.
	 */
	public ChartSpec() {
		this(null);
	}
	
	public ChartSpec(String priorUUID) {
		super(priorUUID);
	}

	public String getParent() {
		return parent;
	}


	public void setDomainAxisName(String newValue) {
		domainAxisName = newValue;

	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setPlotType(PlotTypes newValue) {
		plotType = newValue;
	}

	public void setRangeAxisName(String name) {
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

	public void setDomainTransform(AbstractDisplayTransform newTransform) {
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

	public void setxMax(double xMax) {this.xMax = xMax;}

	public String getDisplayName() {
		if (this.displayName!=null){
			return displayName;
		} else {
			return ID;
		}
	}
	
	
}
// EOF