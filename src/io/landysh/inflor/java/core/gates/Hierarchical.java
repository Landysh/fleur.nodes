package io.landysh.inflor.java.core.gates;

import java.io.Serializable;

public interface Hierarchical extends Serializable{
  
  public String getParentID();
  public String getID();
  public void setParentID(String newValue);

}
