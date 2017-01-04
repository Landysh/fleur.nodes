package main.java.inflor.knime.nodes.compensation.calculate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import main.java.inflor.core.compensation.MatrixCalculator;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.FCSUtilities;

public class CalculateCompensationNodeSettings {
  
  static final String MSG_CHOOSE_VALID_FOLDER = "Please choose a folder containing a set of compensation controls.";
  // Folder containing FCS Files.
  static final String DEFAULT_PATH = "Select...";
  static final String KEY_PATH = "Path";
  static final String KEY_MAP_KEYS = "Compensation Map Keys";
  static final String KEY_MAP_VALUES = "Compensation Map Values";
  static final String KEY_INPUT_DIMENSIONS = "Input Dimensions";
  static final String KEY_OUTPUT_DIMENSIONS = "Output Dimensions";
  static final String KEY_REMOVED_DIMENSIONS = "Ignored Dimensions";
  
  String mPath = DEFAULT_PATH;
  
  MatrixCalculator compCalculator;
    
  public String getPath() {
    return mPath;
  }
  
  public void setPath(String newPath) {
    mPath = newPath; 
    List<FCSFrame> dataList = FCSUtilities.readValidFiles(newPath);
    compCalculator = new MatrixCalculator(dataList);
  }

  public void save(NodeSettingsWO settings) {
    settings.addString(KEY_PATH, mPath);
    int i=0;
    Map<String, FCSFrame> mCompMap = compCalculator.getCompMap();
    String[] mapKeys = new String[mCompMap.size()];
    String[] mapValues = new String[mCompMap.size()];
    for (Entry<String, FCSFrame> entry:mCompMap.entrySet()){
      mapKeys[i] = entry.getKey();
      mapValues[i] = entry.getValue().getPrefferedName();
      i++;
    }
    settings.addStringArray(KEY_MAP_KEYS, mapKeys);
    settings.addStringArray(KEY_MAP_VALUES, mapValues);
    settings.addStringArray(KEY_INPUT_DIMENSIONS, compCalculator.getInputDims());
    settings.addStringArray(KEY_OUTPUT_DIMENSIONS, compCalculator.getOutputDims());
    settings.addStringArray(KEY_REMOVED_DIMENSIONS, compCalculator.getRemovedDims());
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mPath = settings.getString(KEY_PATH);
    
    List<FCSFrame> dataList = FCSUtilities.readValidFiles(mPath);
    
    if (!mPath.equals(DEFAULT_PATH)){
      
      String[] mapKeys = settings.getStringArray(KEY_MAP_KEYS);
      if (mapKeys.length>0){
        String[] mapValues = settings.getStringArray(KEY_MAP_VALUES);
        HashMap<String, String> mCompMap = new HashMap<>();
        for (int i=0;i<mapKeys.length;i++){
          mCompMap.put(mapKeys[i], mapValues[i]);
        }
        
        List<String> inDimensions = Arrays.asList(settings.getStringArray(KEY_INPUT_DIMENSIONS));
        List<String> outDimensions = Arrays.asList(settings.getStringArray(KEY_OUTPUT_DIMENSIONS));
        
        compCalculator = new MatrixCalculator(dataList, mCompMap, inDimensions, outDimensions);
      } else {
        compCalculator = new MatrixCalculator(dataList);
      }
    } else {
      throw new InvalidSettingsException(MSG_CHOOSE_VALID_FOLDER);
    }
  }

  public void removeDimension(String dimension) {
    compCalculator.removeCompDimension(dimension);
  }
  
  public void modifyDimension(String dimension, String newValue) {
    compCalculator.overrideMapping(dimension, newValue);
  }

  public Map<String, FCSFrame> getCompMap() {
    return compCalculator.getCompMap();
  }
  
  public List<String> getFileNames(){
    return compCalculator.getPossibleFilesNames();
  }

  public String[] getInDims() {
    return compCalculator.getInputDims();
  }
  
  public String[] getOutDims() {
    return compCalculator.getInputDims();
  }

  public String[] getFormattedOutDims() {
    String[] formattedDimensionNames = new String[getOutDims().length];
    for (int i=0;i<getInDims().length;i++){
      formattedDimensionNames[i] = "[" + getOutDims()[i] + "]";
    }
    return formattedDimensionNames;
  }
}