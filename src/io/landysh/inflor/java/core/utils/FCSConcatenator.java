package io.landysh.inflor.java.core.utils;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;

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
