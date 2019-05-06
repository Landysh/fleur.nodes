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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jfree.chart.JFreeChart;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.image.png.PNGImageCell;
import org.knime.core.data.image.png.PNGImageCellFactory;
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

import fleur.core.data.FCSFrame;
import fleur.core.transforms.AbstractTransform;
import fleur.core.transforms.TransformSet;
import inflor.core.plots.CategoryResponseChart;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;
import inflor.knime.data.type.cell.fcs.FCSFrameMetaData;

/**
 * This is the model implementation of Transform.
 * 
 *
 * @author Aaron Hart
 */
public class TransformNodeModel extends NodeModel {

  private static final NodeLogger logger = NodeLogger.getLogger(TransformNodeModel.class);
  private static final String DIMENSION_NAMES_COLUMN_NAME = "Dimension Names";
  private static final String TRANSFORM_DETAILS_COLUMN_NAME = "Transform Details";
  private static final String TRANSFORM_PLOT_COLUMN_NAME = "Transform Plot";

  private TransformNodeSettings modelSettings = new TransformNodeSettings();

  private int subtaskIndex;

  /**
   * Constructor for the node model.
   */
  protected TransformNodeModel() {
    super(1, 2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {
    // Create the output spec and data container.
    exec.setProgress(0.01, "Initializing execution");
    DataTableSpec[] outSpecs = createSpecs(inData[0].getSpec());
    BufferedDataContainer container = exec.createDataContainer(outSpecs[0]);
    String columnName = modelSettings.getSelectedColumn();
    int columnIndex = outSpecs[0].findColumnIndex(columnName);

    // Collect the input data.
    exec.setMessage("Reading data");
    ExecutionContext readExec = exec.createSubExecutionContext(0.01);
    ArrayList<FCSFrameFileStoreDataCell> dataSet = new ArrayList<>();
    int rowIndex = 0;
    for (final DataRow inRow : inData[0]) {
      FCSFrameFileStoreDataCell cell = (FCSFrameFileStoreDataCell) inRow.getCell(columnIndex);
      dataSet.add(cell);
      readExec.setProgress((double) rowIndex / (double) inData[0].size(),
          "Reading: " + cell.getFCSFrameMetadata().getDisplayName());
      rowIndex++;
    }

    // create a summary frame containing merged data from all files.
    List<FCSFrame> filteredData;
    // Filter it down to reference subset
    exec.setMessage("filtering data");
    ExecutionContext filterExec = exec.createSubExecutionContext(0.5);
    String referenceSubset = modelSettings.getReferenceSubset();
    //TODO: potential performance hotspot
    if (!referenceSubset.equals(TransformNodeSettings.DEFAULT_REFERENCE_SUBSET)) {
      subtaskIndex = 0;
      filteredData = dataSet
            .parallelStream()
            .map(cell -> filterDataFrame(filterExec, cell.getFCSFrameValue(), referenceSubset, dataSet.size()))
            .peek(df -> {
              filterExec.setProgress(subtaskIndex/dataSet.size());
              subtaskIndex++;
            })
            .collect(Collectors.toList());
    } else {
      subtaskIndex = 0;
        filteredData = dataSet
            .parallelStream()
            .map(cell -> cell.getFCSFrameValue())
            .peek(df -> {
              filterExec.setProgress(subtaskIndex/dataSet.size());
              subtaskIndex++;
            })
            .collect(Collectors.toList());
    }
    // Init and Optimize the transform, record results.
    TransformSet transforms = new TransformSet();
    exec.setMessage("Optimizing transforms");
    exec.setProgress(0.6);
    ExecutionContext optimizeExec = exec.createSubExecutionContext(0.75);
    BufferedDataContainer summaryContainer = exec.createDataContainer(outSpecs[1]);
    subtaskIndex = 0;
    Set<String> parameterSet = filteredData.stream().map(df -> df.getDimensionNames())
        .flatMap(List::stream).collect(Collectors.toSet());
    parameterSet.parallelStream().forEach(name -> transforms.optimizeTransform(name, filteredData));
    
    transforms.getMap().entrySet().parallelStream().forEach(entry -> applyTransform(filteredData,
        entry, summaryContainer, optimizeExec, transforms.getMap().size()));
    summaryContainer.close();

    // write the output table.
    exec.setMessage("Writing output");
    ExecutionContext writeExec = exec.createSubExecutionContext(1);
    subtaskIndex = 0;
    for (final DataRow inRow : inData[0]) {
      final DataCell[] outCells = new DataCell[inRow.getNumCells()];
      FCSFrameFileStoreDataCell fileCell = (FCSFrameFileStoreDataCell) inRow.getCell(columnIndex);
      writeExec.setProgress(subtaskIndex / (double) inData[0].size(),
          fileCell.getFCSFrameMetadata().getDisplayName());
      // now create the output row
      FCSFrameMetaData newMetaData = fileCell
        .getFCSFrameMetadata().copy();
      
      newMetaData.setTransforms(transforms);
      
      for (int j = 0; j < outCells.length; j++) {
        if (j == columnIndex) {
          outCells[j] = new FCSFrameFileStoreDataCell(fileCell.getFileStore(), newMetaData);
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      final DataRow outRow = new DefaultRow("Row " + subtaskIndex, outCells);
      container.addRowToTable(outRow);
      subtaskIndex++;
    }
    container.close();

    BufferedDataTable table = container.getTable();

    String key = NodeUtilities.KEY_TRANSFORM_MAP;
    String value = transforms.saveToString();
    Map<String, String> newProps = new HashMap<>();
    newProps.put(key, value);
    BufferedDataTable finalTable =
        NodeUtilities.addPropertyToColumn(exec, table, columnName, newProps);

    return new BufferedDataTable[] {finalTable, summaryContainer.getTable()};
  }

  private FCSFrame filterDataFrame(ExecutionContext filterExec, FCSFrame df, String subsetName,
      int size) {
    filterExec.setProgress((double) subtaskIndex / size, df.getDisplayName());
    subtaskIndex++;
    return FCSUtilities.filterFrame(df.getFilteredFrame(subsetName, true), df);
  }

  private void applyTransform(List<FCSFrame> flowFrames, Entry<String, AbstractTransform> entry,
      BufferedDataContainer transformSummaryContainer, ExecutionContext optimizeExec,
      Integer size) {
    optimizeExec.setProgress((double) subtaskIndex / size, "Creating plot for: " + entry.getKey());
    AbstractTransform at = entry.getValue();
    DataCell[] cells;
    try {
      byte[] imageBytes = createTransformPlot(flowFrames, entry);
      DataCell imageCell = PNGImageCellFactory.create(imageBytes);
      cells = new DataCell[] {new StringCell(at.getType().toString()),
          new StringCell(at.getDetails()),imageCell};
    } catch (Exception e) {
      cells = new DataCell[] {new StringCell(at.getType().toString()),
          new StringCell(at.getDetails()), new MissingCell("debug")};
      logger.error("Unable to create image cell.", e);
    }
    
    DataRow row = new DefaultRow(entry.getKey(), cells);
    synchronized (transformSummaryContainer) {
      transformSummaryContainer.addRowToTable(row);
    }
  }

  private byte[] createTransformPlot(List<FCSFrame> cytFrames,
      Entry<String, AbstractTransform> entry) throws IOException {
    CategoryResponseChart chart = new CategoryResponseChart(entry.getKey(), entry.getValue());
    JFreeChart jfc = chart.createChart(cytFrames);
    jfc.setBackgroundPaint(Color.WHITE);
    BufferedImage objBufferedImage = jfc.createBufferedImage(400, 300);
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    ImageIO.write(objBufferedImage, "png", bas);
    return bas.toByteArray();
  }

  private DataTableSpec[] createSpecs(DataTableSpec spec) {
    DataTableSpec dataTableSpec = new DataTableSpecCreator(spec).createSpec();
    DataTableSpecCreator transformDetailsCreator = new DataTableSpecCreator();
    DataColumnSpec dimensionNamesColumn =
        new DataColumnSpecCreator(DIMENSION_NAMES_COLUMN_NAME, StringCell.TYPE).createSpec();
    DataColumnSpec detailsColumn =
        new DataColumnSpecCreator(TRANSFORM_DETAILS_COLUMN_NAME, StringCell.TYPE).createSpec();
    DataColumnSpec summaryPlotColumn =
        new DataColumnSpecCreator(TRANSFORM_PLOT_COLUMN_NAME, DataType.getType(PNGImageCell.class))
            .createSpec();
    DataColumnSpec[] transformDetailColumns =
        new DataColumnSpec[] {dimensionNamesColumn, detailsColumn, summaryPlotColumn};
    transformDetailsCreator.addColumns(transformDetailColumns);

    return new DataTableSpec[] {dataTableSpec, transformDetailsCreator.createSpec()};
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
    if (modelSettings.getSelectedColumn() == null) {
      Arrays.asList(inSpecs[0].getColumnNames()).stream().map(inSpecs[0]::getColumnSpec)
          .filter(spec -> spec.getType().equals(FCSFrameFileStoreDataCell.TYPE)).findFirst()
          .ifPresent(column -> modelSettings.setSelectedColumn(column.getName()));
    }
    return createSpecs(inSpecs[0]);
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
      throws IOException, CanceledExecutionException {/* noop */}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/* noop */}
}
