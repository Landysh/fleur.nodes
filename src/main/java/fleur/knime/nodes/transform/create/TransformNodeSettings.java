/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package fleur.knime.nodes.transform.create;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import fleur.core.data.FCSFrame;
import fleur.core.data.Subset;
import fleur.core.transforms.AbstractTransform;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.PlotUtils;

public class TransformNodeSettings {

  // private static final String TRANSFORM_MAP_KEY = "Transfomations";
  // private TreeMap<String, AbstractTransform> mTransforms = new TreeMap<>();


  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  public static final String DEFAULT_REFERENCE_SUBSET = "Ungated";

  private String mSelectedColumn;
  private String mReferenceSubset = DEFAULT_REFERENCE_SUBSET;

  private static final String KEY_REFERENCE_SUBSET = "ReferenceSubset";

  public void save(NodeSettingsWO settings) {
    settings.addString(SELECTED_COLUMN_KEY, mSelectedColumn);
    settings.addString(KEY_REFERENCE_SUBSET, mReferenceSubset);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mSelectedColumn = settings.getString(SELECTED_COLUMN_KEY);
    mReferenceSubset = settings.getString(KEY_REFERENCE_SUBSET);
  }

  public void setSelectedColumn(String selectedItem) {
    mSelectedColumn = selectedItem;
  }

  public String getSelectedColumn() {
    return mSelectedColumn;
  }

  public void validate(NodeSettingsRO settings) {/* noop */}

  public void reset() {/* noop */}

//  public void optimizeTransforms(List<FCSFrame> dataSet, List<String> dimensionNames) {
//    List<FCSFrame> filteredData;
//    if (!mReferenceSubset.equals(DEFAULT_REFERENCE_SUBSET)) {
//      filteredData = dataSet.parallelStream().map(df -> {
//        Subset s = FCSUtilities.findCompatibleSubset(df, mReferenceSubset);
//        List<Subset> ancestry = s.findAncestors(df.getSubsets());
//        BitSet mask = s.evaluate(ancestry);
//        return FCSUtilities.filterFrame(mask, df);
//      }).collect(Collectors.toList());
//    } else {
//      filteredData = dataSet;
//    }
//    
//    TreeMap<String, AbstractTransform> transformMap = new TreeMap<>();
//
//    
//    for (String name : dimensionNames) {
//      Double range = dataSet.stream()
//          .filter(tdf -> tdf.getDimensionNames().contains(name))
//          .map(df -> df.getDimension(name).getRange())
//          .findAny()
//          .get(); //Assumes all ranges are the same.  Could be false.
//      
//      transformMap.put(name, PlotUtils.createDefaultTransform(name,range));
//    }
//
//    transformMap.
//    
//  }

  public void setReferenceSubset(String selectedItem) {
    mReferenceSubset = selectedItem;
  }

  public String getReferenceSubset() {
    return mReferenceSubset;
  }
}
