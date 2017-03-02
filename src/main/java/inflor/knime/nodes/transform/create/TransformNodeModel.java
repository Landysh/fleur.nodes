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
package inflor.knime.nodes.transform.create;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
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

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.plots.PlotUtils;
import inflor.core.plots.CategoryResponseChart;
import inflor.core.transforms.AbstractTransform;
import inflor.core.transforms.BoundDisplayTransform;
import inflor.core.transforms.LogicleTransform;
import inflor.core.transforms.LogrithmicTransform;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.MatrixUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

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
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    // Create the output spec and data container.
    exec.setProgress(0.01, "Initializing execution");
    DataTableSpec[] outSpecs = createSpecs(inData[0].getSpec());
    BufferedDataContainer container = exec.createDataContainer(outSpecs[0]);
    String columnName = modelSettings.getSelectedColumn();
    int columnIndex = outSpecs[0].findColumnIndex(columnName);
    
    //Collect the input data.
    exec.setMessage("Reading data");
    ExecutionContext readExec = exec.createSubExecutionContext(0.25);
    ArrayList<FCSFrame> dataSet = new ArrayList<>();       
    int rowIndex = 0;
    for (final DataRow inRow : inData[0]) {
      FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) inRow.getCell(columnIndex)).getFCSFrameValue();
      dataSet.add(dataFrame.deepCopy());
      readExec.setProgress((double)rowIndex/inData[0].size(), "Reading: " + dataFrame.getDisplayName());
      rowIndex++;
    }
    
    //Filter it down to reference subset
    exec.setMessage("filtering data");
    ExecutionContext filterExec = exec.createSubExecutionContext(0.5);
    String referenceSubset = modelSettings.getReferenceSubset();
    List<FCSFrame> filteredData;
    if (!referenceSubset.equals(TransformNodeSettings.DEFAULT_REFERENCE_SUBSET)){
      subtaskIndex = 0;
      filteredData = dataSet
      .parallelStream()
      .map(df -> filterDataFrame(filterExec, df, referenceSubset, dataSet.size()))
      .collect(Collectors.toList());
    } else {
      filteredData = dataSet;
    }
    
    //Create Default Transforms.
    String[] dimensionNames = inData[0]
        .getSpec()
        .getColumnSpec(modelSettings.getSelectedColumn())
        .getProperties()
        .getProperty(NodeUtilities.DIMENSION_NAMES_KEY)
        .split(NodeUtilities.DELIMITER_REGEX);
    
    Map<String, AbstractTransform> transformMap = Arrays
      .asList(dimensionNames)
      .stream()
      .collect(Collectors.toMap(name -> name, PlotUtils::createDefaultTransform));

    //Optimize the transform and record results.
    exec.setMessage("Optimizing transforms");
    ExecutionContext optimizeExec = exec.createSubExecutionContext(0.75);
    BufferedDataContainer summaryContainer = exec.createDataContainer(outSpecs[1]);
    subtaskIndex = 0;
    transformMap
      .entrySet()
      .parallelStream()
      .forEach(entry -> optimizeTransform(filteredData, entry, summaryContainer, optimizeExec, transformMap.size()));
    summaryContainer.close();
    
    //write the output table.
    exec.setMessage("Writing output");
    ExecutionContext writeExec = exec.createSubExecutionContext(1);
    subtaskIndex = 0;
    for (final DataRow inRow : inData[0]) {
      final DataCell[] outCells = new DataCell[inRow.getNumCells()];
      final FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) inRow.getCell(columnIndex)).getFCSFrameValue().deepCopy();
      writeExec.setProgress(subtaskIndex/(double)inData[0].size(), dataFrame.getDisplayName());
      // now create the output row
      final FCSFrame df = applyTransforms(dataFrame, transformMap);
      final String fsName = NodeUtilities.getFileStoreName(df);
      FileStore fs = fileStoreFactory.createFileStore(fsName);
      int size = NodeUtilities.writeFrameToFilestore(df, fs);
      final FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fs, df, size);

      for (int j = 0; j < outCells.length; j++) {
        if (j == columnIndex) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      final DataRow outRow = new DefaultRow("Row " + subtaskIndex, outCells);
      container.addRowToTable(outRow);
      subtaskIndex++;
    }
    container.close();
    return new BufferedDataTable[] {container.getTable(), summaryContainer.getTable()};
  }

  private FCSFrame filterDataFrame(ExecutionContext filterExec, FCSFrame df, String subsetName, int size) {
    filterExec.setProgress((double) subtaskIndex/size, df.getDisplayName());
    subtaskIndex++;
    return FCSUtilities.filterFrame(df.getFilteredFrame(subsetName, true), df);
  }

  private void optimizeTransform(List<FCSFrame> filteredData, Entry<String, AbstractTransform> entry, 
      BufferedDataContainer transformSummaryContainer, ExecutionContext optimizeExec, Integer size) {
    optimizeExec.setProgress((double) subtaskIndex/size, "Optimizing transform for: " +entry.getKey());
    double[] data = mergeData(entry.getKey(), filteredData);
    AbstractTransform at = entry.getValue();
    if (entry.getValue() instanceof LogicleTransform) {
      LogicleTransform logicle = (LogicleTransform) at;
      logicle.optimizeW(data);
    } else if (entry.getValue() instanceof LogrithmicTransform) {
      LogrithmicTransform logTransform = (LogrithmicTransform) at;
      logTransform.optimize(data);
    } else if (entry.getValue() instanceof BoundDisplayTransform) {
      BoundDisplayTransform boundaryTransform = (BoundDisplayTransform) at;
      boundaryTransform.optimize(data);
    }
    
    byte[] imageBytes = createTransformPlot(filteredData, entry, at);
    try {
      DataCell imageCell =  PNGImageCellFactory.create(imageBytes);
      DataCell[] cells = new DataCell[]{new StringCell(at.getType().toString()), new StringCell(at.getDetails()), imageCell};
      DataRow row = new DefaultRow(entry.getKey(), cells);
      synchronized (transformSummaryContainer) {
        transformSummaryContainer.addRowToTable(row);
      }
    } catch (IOException e) {
      logger.error("Unable to create imgage cell.", e);
    } 

  }

  private byte[] createTransformPlot(List<FCSFrame> filteredData,
    Entry<String, AbstractTransform> entry, AbstractTransform at) {
    CategoryResponseChart chart = new CategoryResponseChart(entry.getKey(), at);
    Map<String, FCSDimension> dataModel = createChartData(entry.getKey(), filteredData);
    JFreeChart jfc = chart.createChart(dataModel);
    jfc.setBackgroundPaint(Color.WHITE);
    BufferedImage objBufferedImage = jfc.createBufferedImage(400, 300);
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    try {
      ImageIO.write(objBufferedImage, "png", bas);
    } catch (IOException e) {
      logger.error("Unable to create transform plot.", e);
    }
    
    return bas.toByteArray();
  }

  private Map<String, FCSDimension> createChartData(String key, List<FCSFrame> filteredData) {
    return filteredData
        .stream()
        .collect(Collectors.toMap(FCSFrame::getDisplayName, f -> f.getDimension(key)));
  }

  private double[] mergeData(String shortName, List<FCSFrame> dataSet2) {
    double[] data = null;
    for (FCSFrame frame : dataSet2) {
      Optional<FCSDimension> dimension = FCSUtilities.findCompatibleDimension(frame, shortName);
      data = MatrixUtilities.appendVectors(data, dimension.get().getData());
    }
    return data;
  }
  
  private FCSFrame applyTransforms(FCSFrame dataFrame, Map<String, AbstractTransform> transformMap) {   
    for (Entry<String, AbstractTransform> entry : transformMap.entrySet()) {
      FCSDimension dimension = dataFrame.getDimension(entry.getKey());
      dimension.setPreferredTransform(entry.getValue());
    }
    return dataFrame;
  }

  private DataTableSpec[] createSpecs(DataTableSpec spec) {
    DataTableSpec dataTableSpec = new DataTableSpecCreator(spec).createSpec();
    DataTableSpecCreator transformDetailsCreator = new DataTableSpecCreator();
    DataColumnSpec dimensionNamesColumn = 
        new DataColumnSpecCreator(DIMENSION_NAMES_COLUMN_NAME, StringCell.TYPE).createSpec();
    DataColumnSpec detailsColumn = 
        new DataColumnSpecCreator(TRANSFORM_DETAILS_COLUMN_NAME, StringCell.TYPE).createSpec();
    DataColumnSpec summaryPlotColumn = 
        new DataColumnSpecCreator(TRANSFORM_PLOT_COLUMN_NAME, DataType.getType(PNGImageCell.class)).createSpec();  
    DataColumnSpec[] transformDetailColumns = new DataColumnSpec[]{dimensionNamesColumn, detailsColumn, summaryPlotColumn};
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
    if (modelSettings.getSelectedColumn()==null){
      Arrays.asList(inSpecs[0].getColumnNames())
      .stream()
      .map(inSpecs[0]::getColumnSpec)
      .filter(spec -> spec.getType().equals(FCSFrameFileStoreDataCell.TYPE))
      .findFirst()
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
      throws IOException, CanceledExecutionException {/*noop*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*noop*/}
}
