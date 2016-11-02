package io.landysh.inflor.java.core.subsets;

import java.io.Serializable;
import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class RootSubset extends AbstractSubset implements Serializable {

  /**
   * Root subset class. Used as a root node in a subset tree.
   */
  private static final long serialVersionUID = -8189764506384264612L;
  private static final String ROOT_NAME = "Ungated";
  private FCSFrame data;

  public RootSubset(FCSFrame data) {
    this(null, data);
  }

  public RootSubset(String priorUUID, FCSFrame data) {
    super(priorUUID);
    this.data = data;
    this.members = new BitSet(data.getRowCount());
  }

  @Override
  public BitSet createLocalMask() {
    return this.members;
  }

  @Override
  public FCSFrame getData() {
    return this.data;
  }

  @Override
  public String toString() {
    return ROOT_NAME;
  }

  @Override
  public BitSet createAncestralMask() {
    return members;
    
  }

}
// EOF
