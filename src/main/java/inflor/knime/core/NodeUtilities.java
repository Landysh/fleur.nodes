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
package main.java.inflor.knime.core;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.data.Subset;

public class NodeUtilities {
  
  public static final String DIMENSION_NAMES_KEY = "DIMENSION_NAMES";
  public static final String SUBSET_NAMES_KEY = "SUBSET_NAMES";
  
  public static final String DELIMITER = "||";
  public static final String DELIMITER_REGEX = "\\|\\|";
  private static final String SAVE_SERIALIZABLE_ERROR_MESSAGE = "Failed to save objects";
  public static final String DISPLAY_NAMES_KEY = "DISPLAY_NAMES";
  public static final String SHORT_NAME_KEY = "$PNN";

  private static final Logger LOGGER = Logger.getLogger(NodeUtilities.class.getName());

  
  @SuppressWarnings("unchecked")
  public static Map<String, Serializable> loadMap(NodeSettingsRO settings, String key)
      throws InvalidSettingsException {
    try {
      byte[] chartBytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(chartBytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      return (Map<String, Serializable>) ois.readObject();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Unable to parse map object",e);
      throw new InvalidSettingsException("Unable to parse map object");
    }
  }

  public static void saveSerializable(NodeSettingsWO settings, String key, Serializable ser) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
    oos = new ObjectOutputStream(bos);
    oos.writeObject(ser);
    oos.flush();
    byte[] chartBytes = bos.toByteArray();
    settings.addByteArray(key, chartBytes);
  }
  
  public Map<String, String> createColumnPropertiesContent(List<FCSFrame> dataSet) {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final HashMap<String, String> content = new HashMap<>();
    HashSet<String> shortNames = new HashSet<>();
    List<Map<String, String>> headers = dataSet
        .stream()
        .map(FCSFrame::getKeywords)
        .collect(Collectors.toList());
    
    // Merge all keywords.
    headers
        .forEach(map -> map.entrySet()
            .forEach(entry -> updateContent(content, entry)));

    // Collect all parameter for experiment in one Hashset.
    dataSet
      .stream()
      .map(FCSFrame::getDimensionNames)
      .forEach(shortNames::addAll);
    String dimensionNames = "";
    for (String name : shortNames) {
      dimensionNames = dimensionNames + name + "||";
    }
    dimensionNames = dimensionNames.substring(0, dimensionNames.length() - 2);
    content.put(DIMENSION_NAMES_KEY, dimensionNames);
    return content;
  }
  
  private void updateContent(HashMap<String, String> content, Entry<String, String> entry) {
    if (content.containsKey(entry.getKey())) {
      String currentValue = content.get(entry.getKey());
      currentValue = currentValue + "||" + entry.getValue();
      entry.setValue(currentValue);//TODO: Possible bug?
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
      throw new InvalidSettingsException(e);
    }
  }
  
  @SuppressWarnings("unchecked")//TODO how bad is this?
  public static List<Serializable> loadList(NodeSettingsRO settings, String key) throws InvalidSettingsException {
    try {
      byte[] bytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      return (List<Serializable>) ois.readObject();
    } catch (Exception e) {
      throw new InvalidSettingsException(e);
    }
  }

  public static String getSaveSerializableErrorMessage() {
    return SAVE_SERIALIZABLE_ERROR_MESSAGE;
  }

  public static Serializable loadSerializeable(NodeSettingsRO settings, String key) throws InvalidSettingsException {
    try {
      byte[] bytes = settings.getByteArray(key);
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      ObjectInputStream ois;
      ois = new ObjectInputStream(bis);
      return (Serializable) ois.readObject();
    } catch (Exception e) {
      throw new InvalidSettingsException(e);
    }
  }
}
