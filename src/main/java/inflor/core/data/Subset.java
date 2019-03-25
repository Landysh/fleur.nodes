package inflor.core.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import inflor.core.gates.BitSetAccumulator;
import inflor.core.gates.BitSetOperator;
import inflor.core.proto.FCSFrameProto.Message.Subset.Type;

@SuppressWarnings("serial")
public class Subset extends DomainObject {

  private BitSet members;
  private String parentID;
  private String label;
  private Type subsetType;
  private String overrideID;
  private Double[] descriptors;
  private String[] dimensions;

  public Subset(String label, BitSet mask, String parentID, String priorUUID, Type type,
      String[] dimensions, Double[] descriptors) {
    super(priorUUID);
    this.setMembers(mask);
    this.setParentID(parentID);
    this.setLabel(label);
    subsetType = type;
    this.dimensions = dimensions;
    this.descriptors = descriptors;
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
    List<Subset> localSubset = subsets.stream().collect(Collectors.toList());
    List<Subset> ancestors = new ArrayList<>();
    boolean hasAncestors = true;
    String currentID = this.parentID;
    while (hasAncestors) {
      final String scopedID = currentID;
      Optional<Subset> parent =
          localSubset.stream().filter(ss -> ss.getID().equals(scopedID)).findAny();
      if (parent.isPresent()) {
        currentID = parent.get().getParentID();
        ancestors.add(parent.get());
        localSubset.remove(parent.get());
      } else {
        hasAncestors = false;
      }
    }
    return ancestors;
  }

  public BitSet evaluate(List<Subset> ancestors) {
    ancestors.add(this);
    BitSetAccumulator acc = new BitSetAccumulator(BitSetOperator.AND);
    Optional<BitSet> mask = ancestors.stream().map(Subset::getMembers)
        .reduce(acc);
    if (mask.isPresent()) {
      
      return mask.get();
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    return this.label;
  }

  public String getOverrideID() {
    return overrideID;
  }

  public void setOverrideID(String newValue) {
    overrideID = newValue;
  }

  public Type getType() {
    return subsetType;
  }

  public Double[] getDescriptors() {
    return descriptors;
  }

  public String[] getDimensions() {
    return dimensions;
  }

  public Subset filter(BitSet mask) {
    /**
     * returns a new member set of length mask.cardinality containing the subset values 
     * for each bit set in the input mask.
     */
    BitSet newMembers = new BitSet(mask.cardinality());
    int j = 0;
    for (int i = 0; i < mask.size(); i++) {
      if (mask.get(i)) {
        boolean newval = members.get(i);
        newMembers.set(j,newval);
        j++;
      }
    }
    return new Subset(label, newMembers, parentID, getID(), getType(), dimensions, descriptors);
  }

  public Subset deepCopy() {
    return new Subset(this.label, (BitSet) this.members.clone(), this.parentID, this.getID(), this.getType(), this.dimensions.clone(), this.descriptors.clone());
  }
}
