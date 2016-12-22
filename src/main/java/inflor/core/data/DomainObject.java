package main.java.inflor.core.data;

import java.io.Serializable;
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

  private String uuid;

  public DomainObject(String priorUUID) {
    if (priorUUID == null) {
      uuid = UUID.randomUUID().toString();
    } else {
      uuid = priorUUID;
    }
  }
  
  public boolean matchesID(String id) {
    return this.uuid.equals(id);
  }
  
  public String getID() {
    return this.uuid;
  }
  
  public void setID(String newID){
    this.uuid = newID;
  }
}
