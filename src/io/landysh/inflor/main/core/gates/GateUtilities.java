package io.landysh.inflor.main.core.gates;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;

public class GateUtilities {
  
  public static final String SUMMARY_FRAME_ID = "Summary";
  public static final String UNGATED_SUBSET_ID = "Ungated";
  
  
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
