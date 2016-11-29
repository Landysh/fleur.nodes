package io.landysh.inflor.main.core.plots;

import java.util.List;

import io.landysh.inflor.main.core.dataStructures.DomainObject;
import io.landysh.inflor.main.core.gates.Hierarchical;

public class ChartSpec extends DomainObject implements Hierarchical{

  /**
   * Version 0.1 of the ChartSpec. Once a 1.0 stable release is made any changes that break backward
   * compatibility must increment the serialID
   */
  private static final long serialVersionUID = 7153659120835974973L;

  private static final PlotTypes DEFAULT_CHART_TYPE = PlotTypes.Density;

  private String parentID;
  private PlotTypes plotType = DEFAULT_CHART_TYPE;
  private String domainAxisName;
  private String rangeAxisName;

  private String displayName;

  private List<String> gateIDs;

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
      return this.getID();
    }
  }

  @Override
  public ChartSpec clone() {
    ChartSpec clonedSpec = new ChartSpec(this.getID());
    clonedSpec.parentID = this.parentID;
    clonedSpec.displayName = this.displayName;
    clonedSpec.domainAxisName = this.domainAxisName;
    clonedSpec.rangeAxisName = this.rangeAxisName;
    return clonedSpec;
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

  public void setDomainAxisName(String newValue) {
    domainAxisName = newValue;
  }

  public void setPlotType(PlotTypes newValue) {
    plotType = newValue;
  }

  public void setRangeAxisName(String newValue) {
    rangeAxisName = newValue;
  }

  @Override
  public String getParentID() {
    return this.parentID;
  }

  @Override
  public void setParentID(String newValue) {
    this.parentID = newValue;
  }
  
  public void setGateIDs(List<String> gateIDs){
    this.gateIDs = gateIDs;
  }
  
  public List<String> getGateIDs(){
    return gateIDs;
  }
  
  @Override
  public String toString(){
   return this.getDisplayName() + " : " + this.getID();
  }
}
