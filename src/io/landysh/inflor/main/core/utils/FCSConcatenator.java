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
package io.landysh.inflor.main.core.utils;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;

public class FCSConcatenator implements BinaryOperator<FCSFrame> {

  @Override
  public FCSFrame apply(FCSFrame arg0, FCSFrame arg1) {
    
    HashMap<String, String> mergedHeader = mergeHeaders(arg0.getKeywords(), arg1.getKeywords());
    FCSFrame mergedFrame = new FCSFrame(mergedHeader, arg0.getRowCount() + arg1.getRowCount());
    TreeSet<FCSDimension> mergedData = mergeData(arg0, arg1);
    mergedFrame.setData(mergedData);
    mergedFrame.setPreferredName("Concatenated Frame");
    
    return mergedFrame;
    
  }

  private TreeSet<FCSDimension> mergeData(FCSFrame arg0, FCSFrame arg1) {
    TreeSet<FCSDimension> mergedData = new TreeSet<FCSDimension>();
    
    
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
      mergedDimension.setPreferredTransform(dimension.getPreferredTransform());;
      mergedData.add(mergedDimension);
    }
    return mergedData;
  }

  private HashMap<String, String> mergeHeaders(HashMap<String, String> header1, 
      HashMap<String, String> header2) {
    
    HashMap<String, String> mergedHeader = new HashMap<>();
    for (String key:header1.keySet()){
      mergedHeader.put(key, header1.get(key));
    }
    for (String key:header2.keySet()){
      if (mergedHeader.containsKey(key)){
        mergedHeader.put(key, mergedHeader.get(key) +"||"+header2.get(key));
      } else {
        mergedHeader.put(key, header2.get(key));
      }
    }
    return mergedHeader;
  }
}
