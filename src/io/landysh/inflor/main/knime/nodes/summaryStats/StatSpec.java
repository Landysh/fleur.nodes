package io.landysh.inflor.main.knime.nodes.summaryStats;

import java.io.Serializable;

import io.landysh.inflor.main.core.dataStructures.DomainObject;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;

@SuppressWarnings("serial")
public class StatSpec extends DomainObject implements Serializable{

  private String refDimension;
  private StatType stat;
  private String[] args;
  private String refSubsetID;

  /**
   * 
   * @param dimensionName
   * @param stat
   * @param arguments - A string array containing arguments for a given statistic eg - 
   *    StatType.PERCENTILE args: ["99"]
   *    StatType.Frequency  args: ["ChildSubset", "AncestorSubSet"]
   * @param priorUUID
   */
  public StatSpec(String dimensionName, String referenceSubsetID, StatType stat, String[] arguments, String priorUUID) {
    super(priorUUID);
    this.refDimension = dimensionName;
    this.refSubsetID = referenceSubsetID;
    this.stat = stat;
    this.args = arguments;
  }
  
  public StatSpec(String defaultDimension, String referenceSubsetID, StatType stat, String[] arguments) {
    this(defaultDimension, referenceSubsetID, stat, arguments, null);
  }

  public Double evaluate(FCSFrame dataFrame){
    Double result = stat.evaluate(dataFrame, refDimension, args);
    return result;    
  }
  
  @Override
  public StatSpec clone(){
    StatSpec clone = new StatSpec(refDimension, refSubsetID, stat, args, getID());
    return clone;
  }

  public void setStatType(StatType newType) {
    this.stat = newType;    
  }

  public StatType getStatType() {
    return this.stat;
  }

  public void setDimension(String newValue) {
     this.refDimension = newValue;
  }

  public void setPercentile(Object value) {
    this.args = new String[]{value.toString()};
  }

  public void setSubsetID(String newVlalue) {
    this.refDimension = newVlalue;
  }

  public void setFrequencyAncestor(String id) {
    args = new String[]{id};
  }
  
  @Override
  public String toString(){
    return stat.getLabel(refSubsetID, refDimension, args);
  }
  
}
