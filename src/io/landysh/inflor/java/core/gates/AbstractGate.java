package io.landysh.inflor.java.core.gates;

import java.util.BitSet;
import java.util.HashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.dataStructures.DomainObject;

@SuppressWarnings("serial")
public abstract class AbstractGate extends DomainObject {


  public AbstractGate(String priorUUID) {
    super(priorUUID);
  }

  public AbstractGate() {
    this(null);
  }

  /**
   * An abstract class that provides the basis for all gating-ml compliant gates.
   * 
   * @see RangeGate
   */

  protected String id;
  protected String parentID;
  protected HashMap<String, String> custom_info;

  /**
   * 
   * @param data - The input data. Must contain entries for all of this.getDimensionNames().
   * @param eventCount - the number of events in each column
   * @return A boolean array of rowCount.length where true corresponds to being in the gate
   */
  public abstract BitSet evaluate(FCSFrame data);

  public String getId() {
    return id;
  }

  public String getInfo(String name) {
    return custom_info.get(name);
  }

  /**
   * Used to implement hierarchical gating.
   * 
   * @return the id of the gate upon which this gate depends.
   */
  public String getParentID() {
    return parentID;
  }

  public String setInfo(String key, String value) {
    return custom_info.put(key, value);
  }

  public void setParent_id(String parentID) {
    this.parentID = parentID;
  }

  /**
   * Generate a gating-ML 2.0 compliant XML Element for this gate.
   * 
   * @return an org.w3c.dom Element.
   */
  public abstract Element toXMLElement();

  /**
   * Override to validate the gate definition. If gate is invalid, throw an exception.
   * 
   * @throws IllegalStateException
   */
  public abstract void validate() throws IllegalStateException;
  public abstract String getDomainAxisName();
  public abstract String getRangeAxisName();
  public abstract String getLabel();

}
