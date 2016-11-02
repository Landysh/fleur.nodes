package io.landysh.inflor.java.core.dataStructures;

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

  public final String ID;

  public DomainObject(String priorUUID) {
    if (priorUUID == null) {
      ID = UUID.randomUUID().toString();
    } else {
      ID = priorUUID;
    }
  }
  
  public boolean matchesID(String id) {
    return this.ID.equals(id);
  }
}
