package io.landysh.inflor.java.core.plots;

import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.transforms.AbstractTransform;

public class ChartSpec extends DomainObject {

  /**
   * Version 0.1 of the ChartSpec. Once a 1.0 stable release is made any changes that break backward
   * compatibility must increment the serialID
   */
  private static final long serialVersionUID = 7153659120835974973L;

  private static final PlotTypes DEFAULT_CHART_TYPE = PlotTypes.DENSITY;

  private String parent;
  private PlotTypes plotType = DEFAULT_CHART_TYPE;
  private String domainAxisName;
  private String rangeAxisName;

  private String displayName;

  private AbstractTransform rangeTransform;
  private AbstractTransform domainTransform;

  /**
   * @param priorUUID an id previously generated (eg if the object was previously serialized). Set
   *        to null to create a new UUID.
   */
  public ChartSpec() {
    this(null);
  }

  public ChartSpec(String priorUUID) {
    super(priorUUID);
  }

  public String getDisplayName() {
    if (this.displayName != null) {
      return displayName;
    } else {
      return ID;
    }
  }

  @Override
  public ChartSpec clone() {
    ChartSpec clonedSpec = new ChartSpec(this.ID);
    clonedSpec.displayName = this.displayName;
    clonedSpec.domainAxisName = this.displayName;
    clonedSpec.parent = this.parent;
    clonedSpec.plotType = this.plotType;
    clonedSpec.rangeAxisName = this.rangeAxisName;
    return clonedSpec;
  }

  // Getters
  public PlotTypes getPlotType() {
    return plotType;
  }

  public String getDomainAxisName() {
    return domainAxisName;
  }

  public String getRangeAxisName() {
    return rangeAxisName;
  }

  public String getParent() {
    return parent;
  }

  public AbstractTransform getRangeTransform() {
    return rangeTransform;
  }

  public AbstractTransform getDomainTransform() {
    return domainTransform;
  }

  // Setters
  public void setDomainAxisName(String newValue) {
    domainAxisName = newValue;
  }

  public void setParent(String newValue) {
    parent = newValue;
  }

  public void setPlotType(PlotTypes newValue) {
    plotType = newValue;
  }

  public void setRangeAxisName(String newValue) {
    rangeAxisName = newValue;
  }

  public void setRangeTransform(AbstractTransform newValue) {
    rangeTransform = newValue;
  }

  public void setDomainTransform(AbstractTransform newValue) {
    domainTransform = newValue;
  }
}
