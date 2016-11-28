package io.landysh.inflor.java.knime.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class NodeUtilities {
  
  public static final String DIMENSION_NAMES_KEY = "DIMENSION_NAMES_KEY";
  public static final String SUBSET_NAMES_KEY = "SUBSET_NAMES_KEY";
  
  public static final String DELIMITER = "||";
  public static final String DELIMITER_REGEX = "\\|\\|";

  
  
  @SuppressWarnings("unchecked")
  public static Map<String, Serializable> loadMap(NodeSettingsRO settings, String key)
      throws InvalidSettingsException {
    try {
      byte[] chartBytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(chartBytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      Map<String, Serializable> loadedObject = (Map<String, Serializable>) ois.readObject();
      return loadedObject;
    } catch (Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException("Unable to parse map object");
    }
  }

  public static void saveSerializable(NodeSettingsWO settings, String key, Serializable obj) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
    try {
      oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    byte[] chartBytes = bos.toByteArray();
    settings.addByteArray(key, chartBytes);
  }
  
  public HashMap<String, String> createColumnPropertiesContent(List<FCSFrame> dataSet) throws Exception {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final HashMap<String, String> content = new HashMap<String, String>();
    HashSet<String> shortNames = new HashSet<>();
    List<HashMap<String, String>> headers = dataSet
        .stream()
        .map(frame -> frame.getKeywords())
        .collect(Collectors.toList());
    
    // Merge all keywords.
    headers
        .forEach(map -> map.entrySet()
            .forEach(entry -> updateContent(content, entry, shortNames)));

    // Collect all parameter for experiment in one Hashset.
    dataSet
      .stream()
      .map(frame -> frame.getColumnNames())
      .forEach(dimensionList -> shortNames.addAll(dimensionList));
    String dimensionNames = "";
    for (String name : shortNames) {
      dimensionNames = dimensionNames + name + "||";
    }
    dimensionNames = dimensionNames.substring(0, dimensionNames.length() - 2);
    System.out.println(dimensionNames);
    content.put(DIMENSION_NAMES_KEY, dimensionNames);

    return content;
  }
  
  private void updateContent(HashMap<String, String> content, Entry<String, String> entry,
      HashSet<String> shortNames) {
    if (content.containsKey(entry.getKey())) {
      String currentValue = content.get(entry.getKey());
      currentValue = currentValue + "||" + entry.getValue();
    } else {
      content.put(entry.getKey(), entry.getValue());
    }
  }

  public static HashSet<Serializable> loadSet(NodeSettingsRO settings, String key) throws InvalidSettingsException {
    try {
      byte[] bytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      @SuppressWarnings("unchecked")
      HashSet<Serializable> loadedObject = (HashSet<Serializable>) ois.readObject();
      return loadedObject;
    } catch (Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException("Unable to parse object set");
    }
  }
  
  public static List<Serializable> loadList(NodeSettingsRO settings, String key) throws InvalidSettingsException {
    try {
      byte[] bytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      @SuppressWarnings("unchecked")
      List<Serializable> loadedObject = (List<Serializable>) ois.readObject();
      return loadedObject;
    } catch (Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException("Unable to parse object list");
    }
  }
  
  
}
