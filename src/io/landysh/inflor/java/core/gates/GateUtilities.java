package io.landysh.inflor.java.core.gates;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class GateUtilities {
  
  public static BitSet applyGatingPath(FCSFrame dataFrame, List<AbstractGate> gates){
    BinaryOperator<BitSet> accumulator = new BitSetAccumulator(BitSetOperator.AND);
    BitSet mask = new BitSet(dataFrame.getRowCount());
    mask.set(0, mask.size()-1);
    Optional<BitSet> maybeMask = gates
      .parallelStream()
      .map(gate -> gate.evaluate(dataFrame))
      .reduce(accumulator);
    if (maybeMask.isPresent()){
      return maybeMask.get();
    } else {
      System.out.println("DEBUG: Returned empty gating path");
      BitSet allInclusiveSet = new BitSet(dataFrame.getRowCount());
      allInclusiveSet.set(0, allInclusiveSet.size()-1);
      return allInclusiveSet;
    }
  }
}
