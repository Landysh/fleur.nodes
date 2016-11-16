package io.landysh.inflor.java.core.dataStructures;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("serial")
public abstract class DomainObject implements Serializable {

  /**
   * An abstract class used to create persistent and identifiable objects. Responsible for the
   * creation of UUIDs and SERDE
   * 
   * @Param priorUUID - a previously generated ID. Use null to create a new ID if a suitable one is
   *        not available.
   */

  private String ID;

  public DomainObject(String priorUUID) {
    if (priorUUID == null) {
      Random rnjesus = new Random();
      ID = Integer.toString(rnjesus.nextInt(1000));
      //ID = UUID.randomUUID().toString();
    } else {
      ID = priorUUID;
    }
  }
  
  public boolean matchesID(String id) {
    return this.ID.equals(id);
  }
  
  public String getID() {
    return this.ID;
  }
  
  public void setID(String newID){
    this.ID = newID;
  }
}
