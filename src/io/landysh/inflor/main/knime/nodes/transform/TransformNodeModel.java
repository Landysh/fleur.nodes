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
package io.landysh.inflor.main.knime.nodes.transform;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.main.core.data.FCSDimension;
import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.plots.PlotUtils;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.utils.FCSUtilities;
import io.landysh.inflor.main.knime.core.NodeUtilities;
import io.landysh.inflor.main.knime.data.type.cell.fcs.*;

/**
 * This is the model implementation of Transform.
 * 
 *
 * @author Aaron Hart
 */
public class TransformNodeModel extends NodeModel {

  private static final NodeLogger logger = NodeLogger.getLogger(TransformNodeModel.class);

  private TransformNodeSettings modelSettings = new TransformNodeSettings();

  /**
   * Constructor for the node model.
   */
  protected TransformNodeModel() {
    super(1, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {
    logger.info("Executing: Create Gates");
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

    // Create the output spec and data container.
    final DataTableSpec outSpec = createSpecs(inData[0].getSpec())[0];
    final BufferedDataContainer container = exec.createDataContainer(outSpec);
    final String columnName = modelSettings.getSelectedColumn();
    final int columnIndex = outSpec.findColumnIndex(columnName);
    //Find a useful set of transforms:
    
    if (modelSettings.getAllTransorms().size()==0){
      createTransformSet(inData, exec, columnIndex);
    }
    
    TreeMap<String, AbstractTransform>  transformSet = modelSettings.getAllTransorms();
       
    
    int i = 0;
    for (final DataRow inRow : inData[0]) {
      final DataCell[] outCells = new DataCell[inRow.getNumCells()];
      final FCSFrame inStore = ((FCSFrameFileStoreDataCell) inRow.getCell(columnIndex)).getFCSFrameValue();
      // now create the output row
      final FCSFrame outStore = applyTransforms(inStore, transformSet);
      final String fsName = i + "ColumnStore.fs";
      final FileStore fileStore = fileStoreFactory.createFileStore(fsName);
      final FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fileStore, outStore);

      for (int j = 0; j < outCells.length; j++) {
        if (j == columnIndex) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      final DataRow outRow = new DefaultRow("Row " + i, outCells);
      container.addRowToTable(outRow);
      i++;
    }
    container.close();
    return new BufferedDataTable[] {container.getTable()};
  }

  private void createTransformSet(BufferedDataTable[] inData, ExecutionContext exec, int columnIndex) {
    List<FCSFrame> fileList = new ArrayList<>();
    for (DataRow inRow : inData[0]) {
      FCSFrame fcsStore = ((FCSFrameFileStoreDataCell) inRow.getCell(columnIndex)).getFCSFrameValue();
      fileList.add(fcsStore);
    }
    modelSettings.optimizeTransforms(fileList);
  }

  private FCSFrame applyTransforms(FCSFrame inStore, TreeMap<String, AbstractTransform> treeMap) {
    for (Entry<String, AbstractTransform> entry : treeMap.entrySet()) {
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(inStore, entry.getKey());
      AbstractTransform optimizedTransform = entry.getValue();
      dimension.setPreferredTransform(optimizedTransform);
    }
    return inStore;
  }

  private DataTableSpec[] createSpecs(DataTableSpec spec) {
    return new DataTableSpec[] {spec};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    modelSettings.reset();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    
    final DataTableSpec spec = inSpecs[0];
    if (modelSettings.getSelectedColumn()==null){
      for (final String name : spec.getColumnNames()) {
        if (spec.getColumnSpec(name).getType() == FCSFrameFileStoreDataCell.TYPE) {
          modelSettings.setSelectedColumn(name);
        }
      }
    }
    DataColumnSpec selectedColumnSpec = spec.getColumnSpec(modelSettings.getSelectedColumn());
    String shortNames = selectedColumnSpec.getProperties()
        .getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    String[] dimensionNames = shortNames.split(NodeUtilities.DELIMITER_REGEX);
    for (String name : dimensionNames) {
      if (modelSettings.getTransform(name) == null) {
        modelSettings.addTransform(name, PlotUtils.createDefaultTransform(name));
      }
    }

    return new DataTableSpec[] {inSpecs[0]};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    modelSettings.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    modelSettings.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    modelSettings.validate(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}
}
