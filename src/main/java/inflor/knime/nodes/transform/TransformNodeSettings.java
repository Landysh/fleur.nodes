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
package main.java.inflor.knime.nodes.transform;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.transforms.BoundDisplayTransform;
import main.java.inflor.core.transforms.LogicleTransform;
import main.java.inflor.core.transforms.LogrithmicTransform;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.core.utils.MatrixUtilities;
import main.java.inflor.knime.core.NodeUtilities;

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
    m_transforms = new TreeMap<>();
    for (Entry<String, Serializable> e : serMap.entrySet()) {
      if (e.getValue() instanceof AbstractTransform){
        m_transforms.put(e.getKey(), (AbstractTransform) e.getValue());
      }
    }
    int i=0;
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
    m_transforms.clear();
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
