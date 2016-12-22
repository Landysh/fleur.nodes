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
package main.java.inflor.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;

public class FCSConcatenator implements BinaryOperator<FCSFrame> {

  @Override
  public FCSFrame apply(FCSFrame arg0, FCSFrame arg1) {
    
    Map<String, String> mergedHeader = mergeHeaders(arg0.getKeywords(), arg1.getKeywords());
    FCSFrame mergedFrame = new FCSFrame(mergedHeader, arg0.getRowCount() + arg1.getRowCount());
    TreeSet<FCSDimension> mergedData = mergeData(arg0, arg1);
    mergedFrame.setData(mergedData);
    mergedFrame.setPreferredName("Concatenated Frame");
    
    return mergedFrame;
    
  }

  private TreeSet<FCSDimension> mergeData(FCSFrame arg0, FCSFrame arg1) {
    TreeSet<FCSDimension> mergedData = new TreeSet<>();
    
    
    for (FCSDimension dimension: arg0.getData()){
      FCSDimension secondDimension = FCSUtilities.findCompatibleDimension(arg1, dimension.getShortName());
      FCSDimension mergedDimension = new FCSDimension(dimension.getSize()+secondDimension.getSize(), 
          dimension.getIndex(), 
          dimension.getShortName(), 
          dimension.getStainName(), 
          dimension.getPNEF1(),
          dimension.getPNEF2(), 
          dimension.getRange()) ;
      double[] mergedArray = MatrixUtilities.appendVectors(dimension.getData(), secondDimension.getData());
      mergedDimension.setData(mergedArray);
      mergedDimension.setPreferredTransform(dimension.getPreferredTransform());
      mergedData.add(mergedDimension);
    }
    return mergedData;
  }

  private Map<String, String> mergeHeaders(Map<String, String> header1, 
      Map<String, String> header2) {
    
    HashMap<String, String> mergedHeader = new HashMap<>();
    for (Entry<String, String> entry:header1.entrySet()){
      mergedHeader.put(entry.getKey(), entry.getValue());
    }
    for (Entry<String, String> entry:header2.entrySet()){
      if (mergedHeader.containsKey(entry.getKey())){
        mergedHeader.put(entry.getKey(), mergedHeader.get(entry.getKey()) +"||"+entry.getValue());
      } else {
        mergedHeader.put(entry.getKey(), entry.getValue());
      }
    }
    return mergedHeader;
  }
}
