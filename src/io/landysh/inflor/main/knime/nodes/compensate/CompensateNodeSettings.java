package io.landysh.inflor.main.knime.nodes.compensate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.main.knime.core.NodeUtilities;

public class CompensateNodeSettings {

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private static final String SPILLOVER_KEY = "Complensation reference";

  private String mSelectedColumn;
  private Map<String, String> mSpilloverReference;

  CompensateNodeSettings() {}

  public void save(NodeSettingsWO settings) {
    NodeUtilities.saveSerializable(settings, SPILLOVER_KEY, (Serializable) mSpilloverReference);
    settings.addString(SELECTED_COLUMN_KEY, mSelectedColumn);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mSelectedColumn = settings.getString(SELECTED_COLUMN_KEY);
    Map<String, Serializable> map = NodeUtilities.loadMap(settings, SPILLOVER_KEY);
    mSpilloverReference = new HashMap<>();
    for (Entry<String, Serializable> e : map.entrySet()) {
      mSpilloverReference.put(e.getKey(), (String) e.getValue());
    }
  }

  public String getSelectedColumn() {
    return mSelectedColumn;
  }

  public void setSelectedColumn(String selectedItem) {
    mSelectedColumn = selectedItem;
  }

  public void setHeader(Map<String, String> keywords) {
    mSpilloverReference = keywords;
  }

  public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
    if (settings.getString(SELECTED_COLUMN_KEY) == null){
      throw new InvalidSettingsException("Selected Column is null");
    } else if (NodeUtilities.loadMap(settings, SPILLOVER_KEY)==null){
      throw new InvalidSettingsException("Spillover reference is null");
    }
  }

  public Map<String, String> getReferenceHeader() {
    return mSpilloverReference;
  }
}
