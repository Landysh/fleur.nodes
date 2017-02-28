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
package inflor.core.gates;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import inflor.core.data.FCSFrame;
import inflor.knime.core.NodeUtilities;

public class GateUtilities {
  
  //public static final String SUMMARY_FRAME_ID = "Summary";
  public static final String UNGATED_SUBSET_ID = "Ungated";
  private static String currentID;
  
  private GateUtilities(){}
  
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
      BitSet allInclusiveSet = new BitSet(dataFrame.getRowCount());
      allInclusiveSet.set(0, allInclusiveSet.size()-1);
      return allInclusiveSet;
    }
  }

  public static AbstractGate[] findAncestors(String terminalID, List<AbstractGate> gates) {
    //TODO: Ask bernd why this can be a field but not a variable.
    currentID = terminalID;
    List<AbstractGate> workingList = gates.stream().collect(Collectors.toList());
    ArrayList<AbstractGate> ancestors = new ArrayList<>();
    while(currentID!=null){
      Optional<AbstractGate> parentGate = 
          workingList
          .stream()
          .filter(gate -> gate.getParentID().equals(currentID))
          .findAny();
      
      if (parentGate.isPresent()){
        currentID = parentGate.get().getID();
        ancestors.add(parentGate.get());
      } else {
        currentID = null;
      }
    }
    currentID = terminalID;
    AbstractGate[] path = new AbstractGate[ancestors.size()];
    for (int i=ancestors.size();i>=0;i--){
      Optional<AbstractGate> gate = gates.stream().filter(g -> g.getID().equals(currentID)).findFirst();
      if (gate.isPresent()){
        path[i] = gate.get();
        currentID = gate.get().getParentID();
      }
    }
    return path;
  }
  
  
  public static String findGatingPath(String terminalID, List<AbstractGate> gates) {
    AbstractGate[] ancestors = GateUtilities.findAncestors(terminalID, gates);
    String[] pathElements = new String[ancestors.length];
    for (int i=0;i<ancestors.length;i++){
      pathElements[i] = ancestors[i].getLabel();
    }
    return String.join(NodeUtilities.DELIMITER, pathElements);
  }
}