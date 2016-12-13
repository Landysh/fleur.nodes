package io.landysh.inflor.main.knime.nodes.experimental.polygonGate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.main.core.gates.AbstractGate;
import io.landysh.inflor.main.core.gates.PolygonGate;
import io.landysh.inflor.main.knime.core.NodeUtilities;

public class PolygonGateSettings {

  private static final String GATES_KEY = "GATES";

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  
  private HashMap<String, PolygonGate> mGates;
  private String mSelectedColumnName;

  public PolygonGateSettings(){
    super();
    mGates = new HashMap<String, PolygonGate>();
  }
  
  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mSelectedColumnName = settings.getString(SELECTED_COLUMN_KEY);
    mGates.clear();
    Map<String, Serializable> serMap = NodeUtilities.loadMap(settings, GATES_KEY);
    for (Entry<String, Serializable> entry:serMap.entrySet()){
      if (entry.getValue() instanceof PolygonGate){
        mGates.put(entry.getKey(), (PolygonGate) entry.getValue()); 
      } else {
        throw new InvalidSettingsException("deserialized entry not of expected type");
      }
    }
  }

  public void save(NodeSettingsWO settings) {
    settings.addString(SELECTED_COLUMN_KEY, mSelectedColumnName);
    NodeUtilities.saveSerializable(settings, GATES_KEY, mGates);
  }
  
  public void addGate(String dataReferenceID, PolygonGate gate){
    mGates.put(dataReferenceID, gate);
  }
  public void removeGate(String dataReferenceID){
    mGates.remove(dataReferenceID);
  }

  public void setSelectedColumn(String selectedItem) {
    mSelectedColumnName = selectedItem;
  }

  public List<AbstractGate> findGates(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSelectedColumn() {
    // TODO Auto-generated method stub
    return null;
  }
}
