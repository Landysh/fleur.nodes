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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.landysh.inflor.main.core.data.FCSDimension;
import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.fcs.ParameterTypes;
import io.landysh.inflor.main.core.singlets.PuleProperties;

public class FCSUtilities {
  
  private FCSUtilities(){
    
  }

  public static Integer findParameterNumnberByName(Map<String, String> keywords, String name) {
    /**
     * Attempts to find the parameter number in a supplied FCS header (keywords). If the parameter
     * name is not it will return null.
     */
    Integer parameterIndex = -1;
    final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));
    for (int i = 1; i <= parameterCount; i++) {
      final String parameterKey = "$P" + i + "N";
      final String value = keywords.get(parameterKey);
      if (value.matches(name)) {
        parameterIndex = i;
        break;
      }
    }
    if (parameterIndex != -1) {
      return parameterIndex;
    } else {
      return null;
    }
  }

  public static String findStainName(Map<String, String> keywords, Integer parameterIndex) {
    /**
     * Returns the $PnS value for a particular parameter index
     */
    String stainName = "";
    try {
      stainName = keywords.get("$P" + (1 + parameterIndex) + "S");
    } catch (final Exception e) {
      System.out.print("No stain name found for parameter index: " + parameterIndex);
      e.printStackTrace();
    }
    return stainName;
  }

  public static double[] filterColumn(BitSet mask, double[] data) {
    double[] filteredData = new double[mask.cardinality()];
    int currentBit = 0;
    for (int i = 0; i < filteredData.length; i++) {
      int nextBit = mask.nextSetBit(currentBit);
      filteredData[i] = data[nextBit];
      currentBit = nextBit + 1;
    }
    return filteredData;
  }

  public static String[] parseDimensionList(Map<String, String> keywords) {
    /**
     * Returns a String[] containing all of the values of the $PnN keywords from the specified
     * header table.
     */
    final int columnCount = Integer.parseInt(keywords.get("$PAR"));
    final String[] plist = new String[columnCount];
    for (int i = 1; i <= columnCount; i++) {
      final String keyword = ("$P" + i + "N");
      plist[i - 1] = keywords.get(keyword);
    }
    return plist;
  }

  public static Boolean validateHeader(Map<String, String> keywords) {
    /**
     * Returns a boolean indicating whether the specified keywords are consistent with the
     * requirements of the FCS Standard version 3.1.
     */
    boolean validHeader = false;
    // Check all required keywords later...
    final boolean fcsVersion = keywords.get("FCSVersion").contains("FCS");
    final Integer rowCount = Integer.parseInt(keywords.get("$TOT"));
    final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));
    if (fcsVersion == true && rowCount > 0 && parameterCount > 0) {
      validHeader = true;
    } else {
      System.out.println("Invalid header: missing required information.");
    }
    return validHeader;
  }

  public static Map<String, String> findParameterKeywords(
      Map<String, String> sourceKeywords, int parameterIndex) {
    HashMap<String, String> keywords = new HashMap<String, String>();
    String regex = "\\$P" + parameterIndex + "[A-Z]";
    for (String key : keywords.keySet()) {
      if (key.matches(regex)) {
        keywords.put(key, sourceKeywords.get(key));
      }
    }
    return keywords;
  }

  public static FCSDimension buildFCSDimension(int pIndex, Map<String, String> header,
      String compensationReference) {
    /**
     * Constructs a new FCSDimension object from the parameter data in the header of an FCS File.
     */
    int size = Integer.parseInt(header.get("$TOT"));
    String pnn = header.get("$P" + pIndex + "N");
    String pns = header.get("$P" + pIndex + "S");
    String pne = header.get("$P" + pIndex + "E");
    double pneF1 = Double.parseDouble(pne.split(",")[0]);
    double pneF2 = Double.parseDouble(pne.split(",")[1]);
    double pnr = Double.parseDouble(header.get("$P" + pIndex + "R"));


    FCSDimension newDimension =
        new FCSDimension(size, pIndex, pnn, pns, pneF1, pneF2, pnr);

    return newDimension;
  }

  public static FCSFrame filterColumnStore(BitSet mask, FCSFrame in) {

    FCSFrame out = new FCSFrame(in.getKeywords(), mask.cardinality());
    for (FCSDimension inDim : in.getData()) {
      FCSDimension outDim = new FCSDimension(mask.cardinality(), inDim.getIndex(),
          inDim.getShortName(), inDim.getStainName(), inDim.getPNEF1(), inDim.getPNEF2(),
          inDim.getRange());
      outDim.setPreferredTransform(inDim.getPreferredTransform());
      outDim.setData(BitSetUtils.filter(inDim.getData(), mask));
      out.addDimension(outDim);
    }
    return out;
  }

  public static FCSDimension findCompatibleDimension(FCSFrame dataSource, String shortName) {
    /**
     * Returns the key for the first compatible FCSDimension in the selected map. (ie. where the
     * result of the toString() method is the same). Will return null if no compatible entry is
     * found.
     */
    FCSDimension returnDim = null;
    for (FCSDimension dim : dataSource.getData()) {
      if (dim.getShortName().equals(shortName)) {
        returnDim = dim;
      }
    }
    return returnDim;
  }

  public static FCSFrame createSummaryFrame(List<FCSFrame> fcsList, Integer maxEventsPerFrame) {
    Integer dataSize = fcsList
      .stream()
      .map(dataFrame -> dataFrame.getRowCount())
      .min(Integer::compare).get();
    final Integer finalSize;
    if (dataSize>maxEventsPerFrame){
      finalSize = maxEventsPerFrame;
    } else {
      finalSize = dataSize;
    }
    
    FCSFrame mergedFrame = fcsList
         .stream()
         .map(dataFrame -> FCSUtilities.downSample(dataFrame, finalSize))
         .reduce(new FCSConcatenator())
         .get(); 
    return mergedFrame;
  }

  private static FCSFrame downSample(FCSFrame dataFrame, Integer dataSize) {
    BitSet mask = BitSetUtils.getShuffledMask(dataFrame.getRowCount(), dataSize);
    return FCSUtilities.filterColumnStore(mask, dataFrame);
  }

  public static FCSDimension findPreferredDimensionType(FCSFrame fcsFrame, ParameterTypes dimensionType) {
    ArrayList<FCSDimension> forwardScatterDims = new ArrayList<FCSDimension>();
    for (FCSDimension dimension: fcsFrame.getData()){
      boolean isForwardScatter = dimensionType.matches(dimension.getShortName());
      if (isForwardScatter){
        forwardScatterDims.add(dimension);
      }
    }
    
    FCSDimension fscADimension = findDimensionType(forwardScatterDims, PuleProperties.AREA);
    FCSDimension fscHDimension = findDimensionType(forwardScatterDims, PuleProperties.HEIGHT);
    FCSDimension fscWDimension = findDimensionType(forwardScatterDims, PuleProperties.WIDTH);
    
    if (fscADimension!=null){
      return fscADimension;
    } else if (fscHDimension!=null){
      return fscHDimension;
    } else if (fscWDimension!=null){
      return fscWDimension;
    } else {
      return null;
    }
  }

  private static FCSDimension findDimensionType(ArrayList<FCSDimension> forwardScatterDims, PuleProperties pulseTypes) {
    FCSDimension foundDimension = null;
    if (forwardScatterDims.size()>=1){
      Optional<FCSDimension> optionalDimension = forwardScatterDims
          .stream()
          .filter(dim -> pulseTypes.matches(dim.getShortName()))
          .findAny();
      if (optionalDimension.isPresent()){foundDimension = optionalDimension.get();}
    }
    return foundDimension;
  }

  public static String formatCompStainName(String origninalName) {
    return "[" + origninalName + "]";
  }

  public static Set<String> getDimensionNames(List<FCSFrame> dataSet) {
    HashSet<String> setDimensionNames = new HashSet<String>();
    for (FCSFrame dataFrame: dataSet){
      Set<String> frameDimensionNames = dataFrame.getData()
          .stream()
          .map(dim -> dim.getShortName())
          .collect(Collectors.toSet());
    setDimensionNames.addAll(frameDimensionNames);
    }
    return setDimensionNames;
  }
}
