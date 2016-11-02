package io.landysh.inflor.java.core.gates;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class GateUtilities {
  
  
  public static BitSet applyGatingPath(FCSFrame dataFrame, List<AbstractGate> gates){
    BinaryOperator<BitSet> accumulator = new BitSetAccumulator(BitSetOperator.AND);
    
    Optional<BitSet> maybeMask = gates
      .parallelStream()
      .map(gate -> gate.evaluate(dataFrame))
      .reduce(accumulator);
    
    if (maybeMask.isPresent()){
      BitSet mask = maybeMask.get();
      return mask;
    } else {
      RuntimeException re = new RuntimeException("Unable to apply gates.");
      re.printStackTrace();
      throw re;
    }

  }
}
