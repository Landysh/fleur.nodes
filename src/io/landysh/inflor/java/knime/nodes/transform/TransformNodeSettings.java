package io.landysh.inflor.java.knime.nodes.transform;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicTransform;
import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.core.utils.MatrixUtilities;
import io.landysh.inflor.java.knime.core.NodeUtilities;

public class TransformNodeSettings {

  private static final String TRANSFORM_MAP_KEY = "Transfomations";
  private static final String SELECTED_COLUMN_KEY = "Selected Column";

  private String m_selectedColumn;
  private TreeMap<String, AbstractTransform> m_transforms =
      new TreeMap<String, AbstractTransform>();

  public TransformNodeSettings() {}

  public void save(NodeSettingsWO settings) {
    settings.addString(SELECTED_COLUMN_KEY, m_selectedColumn);
    NodeUtilities.saveSerializable(settings, TRANSFORM_MAP_KEY, m_transforms);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    m_selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
    Map<String, Serializable> serMap = NodeUtilities.loadMap(settings, TRANSFORM_MAP_KEY);
    TreeMap<String, AbstractTransform> loadedTransforms = new TreeMap<String, AbstractTransform>();
    for (Entry<String, Serializable> e : serMap.entrySet()) {
      if (e.getValue() instanceof AbstractTransform){
        loadedTransforms.put(e.getKey(), (AbstractTransform) e.getValue());
      }
    }
    m_transforms = loadedTransforms;
  }

  public void addTransform(String key, AbstractTransform value) {
    m_transforms.put(key, value);
  }

  public void removeTransform(String key) {
    m_transforms.remove(key);
  }

  public void setSelectedColumn(String selectedItem) {
    m_selectedColumn = selectedItem;
  }

  public String getSelectedColumn() {
    return m_selectedColumn;
  }

  public AbstractTransform getTransform(String name) {
    return m_transforms.get(name);
  }

  public void setTransform(AbstractTransform newValue, String key) {
    this.m_transforms.put(key, newValue);
  }

  public TreeMap<String, AbstractTransform> getAllTransorms() {
    return this.m_transforms;
  }

  public void validate(NodeSettingsRO settings) {
    // TODO Auto-generated method stub
  }

  public void reset() {
    //m_selectedColumn = null;
    m_transforms.clear();;
  }
  
  public void optimizeTransforms(List<FCSFrame> dataSet) {
    for (Entry<String, AbstractTransform> entry : this.getAllTransorms().entrySet()) {
      double[] data = mergeData(entry.getKey(), dataSet);
      if (entry.getValue() instanceof LogicleTransform) {
        LogicleTransform logicle = (LogicleTransform) entry.getValue();
        logicle.optimizeW(data);
      } else if (entry.getValue() instanceof LogrithmicTransform) {
        LogrithmicTransform logTransform = (LogrithmicTransform) entry.getValue();
        logTransform.optimize(data);
      } else if (entry.getValue() instanceof BoundDisplayTransform) {
        BoundDisplayTransform boundaryTransform = (BoundDisplayTransform) entry.getValue();
        boundaryTransform.optimize(data);
      }
    }
  }
  
  private double[] mergeData(String shortName, List<FCSFrame> dataSet2) {
    double[] data = null;
    for (FCSFrame frame : dataSet2) {
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(frame, shortName);
      data = MatrixUtilities.appendVectors(data, dimension.getData());
    }
    return data;
  }
}
