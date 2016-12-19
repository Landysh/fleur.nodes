/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package io.landysh.inflor.main.core.gates;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import io.landysh.inflor.main.core.data.FCSFrame;

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
