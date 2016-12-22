package main.java.inflor.core.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

import main.java.inflor.core.gates.BitSetAccumulator;
import main.java.inflor.core.gates.BitSetOperator;

@SuppressWarnings("serial")
public class Subset extends DomainObject {

  private BitSet members;
  private String parentID;
  private String label;

  public Subset(String label, BitSet mask, String parentID, String priorUUID) {
    super(priorUUID);
    this.setMembers(mask);
    this.setParentID(parentID);
    this.setLabel(label);
  }

  public String getParentID() {
    return parentID;
  }

  public void setParentID(String parentID) {
    this.parentID = parentID;
  }

  public BitSet getMembers() {
    return members;
  }

  public void setMembers(BitSet members) {
    this.members = members;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<Subset> findAncestors(List<Subset> subsets) {
    List<Subset> ancestors = new ArrayList<Subset>();
    boolean hasAncestors = true;
    while(hasAncestors){
      Optional<Subset> parent = subsets
        .stream()
        .filter(ss -> ss.getID().equals(this.getID()))
        .findAny();
      if (parent.isPresent()){
        ancestors.add(parent.get());
      } else {
        hasAncestors = false;
      }
      
    }
    return ancestors;
  }

  public BitSet evaluate(List<Subset> ancestors) {
    ancestors.add(this);
    Optional<BitSet> mask = ancestors
        .stream()
        .map(subset -> subset.getMembers())
        .reduce(new BitSetAccumulator(BitSetOperator.AND));
    if (mask.isPresent()){
      return mask.get();
    } else {
      return null;
    }
  }

  @Override
  public String toString(){
    return this.label;
  }
}
