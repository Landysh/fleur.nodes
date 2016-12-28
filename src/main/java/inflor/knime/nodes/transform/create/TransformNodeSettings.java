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
package main.java.inflor.knime.nodes.transform.create;

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

  private String mSelectedColumn;
  private TreeMap<String, AbstractTransform> mTransforms = new TreeMap<>();

  public void save(NodeSettingsWO settings) {
    settings.addString(SELECTED_COLUMN_KEY, mSelectedColumn);
    NodeUtilities.saveSerializable(settings, TRANSFORM_MAP_KEY, mTransforms);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mSelectedColumn = settings.getString(SELECTED_COLUMN_KEY);
    Map<String, Serializable> serMap = NodeUtilities.loadMap(settings, TRANSFORM_MAP_KEY);
    mTransforms = new TreeMap<>();
    for (Entry<String, Serializable> e : serMap.entrySet()) {
      if (e.getValue() instanceof AbstractTransform){
        mTransforms.put(e.getKey(), (AbstractTransform) e.getValue());
      }
    }
  }

  public void addTransform(String key, AbstractTransform value) {
    mTransforms.put(key, value);
  }

  public void removeTransform(String key) {
    mTransforms.remove(key);
  }

  public void setSelectedColumn(String selectedItem) {
    mSelectedColumn = selectedItem;
  }

  public String getSelectedColumn() {
    return mSelectedColumn;
  }

  public AbstractTransform getTransform(String name) {
    return mTransforms.get(name);
  }

  public void setTransform(AbstractTransform newValue, String key) {
    this.mTransforms.put(key, newValue);
  }

  public TreeMap<String, AbstractTransform> getAllTransorms() {
    return this.mTransforms;
  }

  public void validate(NodeSettingsRO settings) {
    // TODO Auto-generated method stub
  }

  public void reset() {
    mTransforms.clear();
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
