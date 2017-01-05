package main.java.inflor.core.compensation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.ParameterTypes;
import main.java.inflor.core.gates.RangeGate;
import main.java.inflor.core.utils.BitSetUtils;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.knime.nodes.statistics.StatSpec;
import main.java.inflor.knime.nodes.statistics.StatType;

public class TheilSenMatrixCalculator {
  
  private static final String MSG_MAPPED_FILE_NOT_FOUND = "Mapped file not found in supplied data list";
  private static final String MSG_EMPTY_COMP_LIST = "Input list should contain at least 1 FCSFrame";

  private static final Logger LOGGER = Logger.getLogger( TheilSenMatrixCalculator.class.getName() );

  private static final String SUBSET_NAME_SCATTER = "INFLOR_COMP_SCATTER";

  
  private Map<String, FCSFrame> dataMap;
  private boolean isValid = false;
  private List<String> status = new ArrayList<>();
  private ArrayList<String> inDimensions = new ArrayList<>();
  private ArrayList<String> outDimensions = new ArrayList<>();
  private ArrayList<String> ignoredDimensions = new ArrayList<>();
  private List<FCSFrame> dataList;
  
  public TheilSenMatrixCalculator(List<FCSFrame> newDataList){
    dataList = newDataList;
    Optional<FCSFrame> anyFrame = dataList.stream().findAny();
    if (anyFrame.isPresent()){
      ArrayList<String> dimensionNames = anyFrame.get().getDimensionNames();
      ArrayList<String> fluorescentDims = dimensionNames
          .stream()
          .filter(FCSUtilities::isFluorescent)
          .collect(Collectors.toCollection(ArrayList::new));
      
      inDimensions = fluorescentDims;
      outDimensions = fluorescentDims;
      dataMap = initializeDataSet(fluorescentDims, dataList);
      isValid = validate();
    } else {
      throw new IllegalArgumentException(MSG_EMPTY_COMP_LIST);
    }
  }
  
  public TheilSenMatrixCalculator(List<FCSFrame> newDataList, Map<String, String> compensationMap, List<String> inDims, List<String> outDims) {
    dataList = newDataList;

    inDimensions = inDims
        .stream()
        .collect(Collectors.toCollection(ArrayList::new));
    
    outDimensions = outDims
        .stream()
        .collect(Collectors.toCollection(ArrayList::new));
    dataMap = new HashMap<>();
    for (Entry<String, String> entry: compensationMap.entrySet()){
      Optional<FCSFrame> mappedFrame = dataList
          .stream()
          .filter(frame -> frame.getPrefferedName().matches(entry.getValue()))
          .findAny();
      if (mappedFrame.isPresent()){
        dataMap.put(entry.getKey(), mappedFrame.get());
      } else {
        throw new IllegalArgumentException(MSG_MAPPED_FILE_NOT_FOUND);
      }
    }
    isValid = validate();    
  }

  private boolean validate() {
    boolean hasData = !dataMap.isEmpty();
    boolean hasValidDimensions = validateData(dataMap);
    return hasData&&hasValidDimensions;
  }

  private boolean validateData(Map<String, FCSFrame> dataSet2) {
    status.clear();
    boolean valid = true;
    for (Entry<String, FCSFrame> e: dataSet2.entrySet()){
      //Check to see that the dimension exists in the dataFrame.
      boolean hasDimension = e.getValue().hasDimension(e.getKey());
      if (hasDimension){
        //noop
      } else {
        valid = valid&&hasDimension;
        status.add("Missing dimension: " + e.getKey() + " in " + e.getValue().getPrefferedName());
      }
      //Check that each file is used only once. 
      for (Entry<String, FCSFrame> e2: dataSet2.entrySet()){
        String e1k = e.getKey();
        String e2k = e2.getKey();
        FCSFrame e1v = e.getValue();
        FCSFrame e2v = e2.getValue();
        boolean equalValues = e2v.equals(e1v);
        boolean equalKeys = e1k.equals(e2k);
        if (equalValues&&(!equalKeys)){
            valid = false;
            status.add("Duplicate entries for: " + e.getKey() + " and: " + e2.getKey() + "->" + e2.getValue().getPrefferedName());
        }
      }
    }
    return valid;
  }

  private Map<String, FCSFrame> initializeDataSet(List<String> fluorescentDims, List<FCSFrame> dataList){
    HashMap<String,FCSFrame> compMap = new HashMap<>();
    for (String s:fluorescentDims){
      FCSFrame matchingFrame = findCompDimension(s, dataList);
      compMap.put(s, matchingFrame);
    }
    return compMap;   
  }
  
  private FCSFrame findCompDimension(String shortName, List<FCSFrame> dataList) {
    for (FCSFrame nameMatchedframe: dataList){
      if (nameMatchedframe.getKeywordValue("$FIL").matches(".*"+shortName+".*")){
        return nameMatchedframe;
      }      
    }
    
    FCSFrame brightestFrame = null;
    Double maxMedian = Double.MIN_VALUE;
    for (FCSFrame frame: dataList){
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(frame, shortName);
      if (dimension!=null){
        StatSpec spec = new StatSpec(shortName, null, StatType.MEDIAN, null);
        Double value = spec.evaluate(frame);
        if (value > maxMedian){
          maxMedian = value;
          brightestFrame = frame;
        }
      }
    }
    if (brightestFrame == null){
      LOGGER.log(Level.FINE, "No matching frame found, null returned.");
    }
    return brightestFrame;
  }

  
  public boolean isValid(){
    return isValid;
  }
  
  public List<String> getStatus(){
    return status;
  }

  public Map<String, FCSFrame> getCompMap() {
    return dataMap;
  }

  public void removeCompDimension(String shorName) {
    dataMap.remove(shorName);
    inDimensions.remove(shorName);
    outDimensions.remove(shorName);
    isValid = validate();
  }
  
  public void overrideMapping(String shortName, String newValue) {
    dataMap.remove(shortName);
    Optional<FCSFrame> newFrame = dataList.stream().filter(frame -> frame.getPrefferedName().equals(newValue)).findAny();
    if (newFrame.isPresent()){
      dataMap.put(shortName, newFrame.get());
    } else {
      LOGGER.log(Level.FINE, newValue + ": not found in loaded data.");
    }
    isValid = validate();
  }

  public double[][] calculate() {
    if (isValid){
      double[][] mtx = new double[outDimensions.size()][inDimensions.size()];
      for (int i=0;i<outDimensions.size();i++){
        mtx[i] = estimateSpillovers(outDimensions.get(i), inDimensions, dataMap.get(outDimensions.get(i)));
      }
      return mtx;
    } else {
      throw new IllegalAccessError("Matrix may not be calculated until isValid() returns true.");
    }
  }

  private double[] estimateSpillovers(String outName, ArrayList<String> inDims, FCSFrame fcsFrame) {
    double[] spills = new double[inDims.size()];
    double[] x = fcsFrame.getFCSDimensionByShortName(outName).getData();
    Percentile p = new Percentile();
    p.setData(x);
    double min = p.evaluate(90.);
    double max = fcsFrame.getFCSDimensionByShortName(outName).getRange(); 
    RangeGate gate = new RangeGate(SUBSET_NAME_SCATTER, new String[]{outName}, new double[]{min}, new double[]{max});
    BitSet mask = gate.evaluate(fcsFrame);
    FCSFrame fcsFrameFilt = FCSUtilities.filterColumnStore(mask, fcsFrame);
    
    int downSize = 1000;
    
    int finalSize = fcsFrameFilt.getRowCount() < downSize ? fcsFrameFilt.getRowCount() : downSize; 
    BitSet dsMask = BitSetUtils.getShuffledMask(fcsFrameFilt.getRowCount(), finalSize);
    FCSFrame finalFrame = FCSUtilities.filterColumnStore(dsMask, fcsFrameFilt);
    double[] filteredX = finalFrame.getFCSDimensionByShortName(outName).getData();
    
    for (int i=0;i<inDims.size();i++){
      String name = inDims.get(i);
      double[] filteredY = finalFrame.getFCSDimensionByShortName(name).getData();
      spills[i] = TheilSenEstimator.evaluate(filteredX, filteredY);
    }
    return spills;
  }

  public String[] getOutputDims() {
    return outDimensions.toArray(new String[outDimensions.size()]);
  }

  public String[] getInputDims() {
    return inDimensions.toArray(new String[inDimensions.size()]);
  }

  public String[] getRemovedDims() {
    return ignoredDimensions.toArray(new String[ignoredDimensions.size()] );
  }
  
  public List<String> getPossibleFilesNames(){
    return dataList
        .stream()
        .map(FCSFrame::getPrefferedName)
        .collect(Collectors.toList());
  }
}