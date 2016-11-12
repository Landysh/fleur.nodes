package io.landysh.inflor.java.core.subsets;

import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;

public class DefaultSubset extends AbstractSubset {

  /**
   * A lightweight and serializable subset definition
   */
  private static final long serialVersionUID = 3267152042593062193L;

  AbstractSubset parentSubset;
  AbstractGate gate;

  public DefaultSubset(AbstractSubset parent, AbstractGate filter) {
    this(null, parent, filter);
  }

  public DefaultSubset(String priorUUID, AbstractSubset parent, AbstractGate filter) {
    super(priorUUID);
    parentSubset = parent;
    gate = filter;
  }

  @Override
  public BitSet createLocalMask() {
    return gate.evaluate(getData());
  }

  @Override
  public FCSFrame getData() {
    return parentSubset.getData();
  }
  
  public boolean matchesID(String ID){
    return this.getID().equals(ID);
  }

  @Override
  public BitSet createAncestralMask() {
    BitSet ancestorMask = parentSubset.createAncestralMask();
    BitSet localMask = gate.evaluate(getData());
    BitSet finalMask = (BitSet) localMask.clone();
    finalMask.and(ancestorMask);
    return finalMask;
  }
}
