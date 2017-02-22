package main.java.inflor.knime.nodes.utility.extract.data;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.BitSetUtils;
import main.java.inflor.knime.core.NodeUtilities;
import main.java.inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;


/**
 * This is the model implementation of ExtractTrainingSet. Extracts data from an FCS frame column to
 * a standard KNIME Table. Data may be transformed and gating information included for downstream ML
 * applications.
 *
 * @author Aaron Hart
 */
public class ExtractDataNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ExtractDataNodeModel.class);



  static final String KEY_SELECTED_COLUMN = "Selected column";
  static final String DEFAULT_COLUMN = "none";
  private SettingsModelColumnName mSelectedColumn =
      new SettingsModelColumnName(KEY_SELECTED_COLUMN, DEFAULT_COLUMN);


  public static final String KEY_TRANSFORM_DATA = "Transform?";
  public static final boolean DEFAULT_TRANSFORM = true;
  private SettingsModelBoolean mTransform =
      new SettingsModelBoolean(KEY_TRANSFORM_DATA, DEFAULT_TRANSFORM);

  public static final String KEY_DOWNSAMPLE_DATA = "Downsample?";
  public static final boolean DEFAULT_DOWNSAMPLE = false;
  private SettingsModelBoolean mDownsample =
      new SettingsModelBoolean(KEY_DOWNSAMPLE_DATA, DEFAULT_DOWNSAMPLE);

  static final String KEY_PERFILE_EVENT_COUNT = "Count";
  static final int DEFAULT_COUNT = 5000;
  static final int MIN_COUNT = 0;
  static final int MAX_COUNT = Integer.MAX_VALUE;

  private SettingsModelIntegerBounded mCount =
      new SettingsModelIntegerBounded(KEY_PERFILE_EVENT_COUNT, DEFAULT_COUNT, MIN_COUNT, MAX_COUNT);



  private int rowIndex;



  private DataTableSpec outputSpec;



  /**
   * Constructor for the node model.
   */
  protected ExtractDataNodeModel() {
    super(1, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {

    logger.info("ExtractTrainingSetNodeModel executing.");

    outputSpec = createSpec(inData[0].getSpec());
    BufferedDataContainer container = exec.createDataContainer(outputSpec);
    int frameIndex = inData[0].getSpec().findColumnIndex(mSelectedColumn.getStringValue());
    rowIndex = 0;
    for (DataRow inRow : inData[0]) {
      FCSFrameFileStoreDataCell cell = (FCSFrameFileStoreDataCell) inRow.getCell(frameIndex);
      FCSFrame dataFrame = cell.getFCSFrameValue();
      writeRows(container, dataFrame);
      // update the progress bar
      exec.checkCanceled();
      exec.setProgress((double) rowIndex / (inData[0].size() * mCount.getIntValue()));
      exec.setMessage("Down sampling: " + dataFrame.getDisplayName());
    }
    container.close();
    BufferedDataTable out = container.getTable();
    return new BufferedDataTable[] {out};
  }

  private void writeRows(BufferedDataContainer container, FCSFrame dataFrame) {
    if (mDownsample.getBooleanValue()) {
      BitSet mask = BitSetUtils.getShuffledMask(dataFrame.getRowCount(), mCount.getIntValue());
      for (int i = 0; i < mask.length(); i++) {
        if (mask.get(i)) {
          badWriteRow(container, dataFrame, i);
        }
      }
    } else {
      for (int i = 0; i < dataFrame.getRowCount(); i++) {
        badWriteRow(container, dataFrame, i);
      }
    }
  }

  private void badWriteRow(BufferedDataContainer container, FCSFrame dataFrame,
      int i) {
    DataCell[] cells;
    if (dataFrame.getSubsets().isEmpty()) {
      cells = writeCellsWithOnlyDimensionColumns(dataFrame, i);
    } else {
      cells = writeCellsWithSubsetColumns(dataFrame, i);
    }
    cells[cells.length - 1] = new StringCell(dataFrame.getDisplayName());
    final RowKey rowKey = new RowKey("Row " + rowIndex);
    
    for (int j=0;j<cells.length;j++){
      if (cells[j] == null){
        cells[j] = new MissingCell(":("); 
      }
    }
    
    final DataRow tableRow = new DefaultRow(rowKey, cells);
    container.addRowToTable(tableRow);
    rowIndex++;
  }

  private DataCell[] writeCellsWithOnlyDimensionColumns(FCSFrame dataFrame, int i) {
    DataCell[] cells;
    double[] dimensionValues = dataFrame.getRow(i, mTransform.getBooleanValue());
    cells = new DataCell[dimensionValues.length + 1];
    for (int j = 0; j < dimensionValues.length; j++) {
      cells[j] = new DoubleCell(dimensionValues[j]);
    }
    return cells;
  }

  private DataCell[] writeCellsWithSubsetColumns(FCSFrame dataFrame, int i) {
    DataCell[] cells;
    double[] dimensionValues = dataFrame.getRow(i, mTransform.getBooleanValue());
    Map<String, Integer> subsetValues = dataFrame.getSubsetRow(i);
    cells = new DataCell[dimensionValues.length + subsetValues.size() + 2];
    for (int j = 0; j < dimensionValues.length; j++) {
      cells[j] = new DoubleCell(dimensionValues[j]);
    }
    for (Entry<String, Integer> e: subsetValues.entrySet()) {
      int columnIndex = outputSpec.findColumnIndex(e.getKey());
      cells[columnIndex] = new IntCell(e.getValue());
    }
    cells[dimensionValues.length+subsetValues.size()] = new StringCell(String.join(",", subsetValues
                                                                                            .entrySet()
                                                                                            .stream()
                                                                                            .filter(e -> e.getValue()==1)
                                                                                            .map(Entry::getKey)
                                                                                            .collect(Collectors.toList())));
    return cells;
  }

  private DataTableSpec createSpec(DataTableSpec inSpec) {
    String columnName = mSelectedColumn.getColumnName();
    DataColumnProperties properties = inSpec.getColumnSpec(columnName).getProperties();
    String rawDimensionNames = properties.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    String[] dimensionNames = rawDimensionNames.split(NodeUtilities.DELIMITER_REGEX);
    String rawDisplayNames = properties.getProperty(NodeUtilities.DISPLAY_NAMES_KEY);
    String[] displayNames = rawDisplayNames.split(NodeUtilities.DELIMITER_REGEX);
    DataColumnSpec[] colSpecs;
    int outColumnCount;

    if (properties.containsProperty(NodeUtilities.SUBSET_NAMES_KEY)) {
      String rawSubsetNames = properties.getProperty(NodeUtilities.SUBSET_NAMES_KEY);
      String[] subsetNames = rawSubsetNames.split(NodeUtilities.DELIMITER_REGEX);
      outColumnCount = subsetNames.length + dimensionNames.length + 2;
      colSpecs = new DataColumnSpec[outColumnCount];
      for (int i = 0; i < dimensionNames.length; i++) {
        DataColumnSpecCreator creator = new DataColumnSpecCreator(displayNames[i], DoubleCell.TYPE);
        Map<String, String> content = new HashMap<>();
        content.put(NodeUtilities.SHORT_NAME_KEY, dimensionNames[i]);
        DataColumnProperties props = new DataColumnProperties(content);
        creator.setProperties(props);
        colSpecs[i] = creator.createSpec();
      }

      for (int i = 0; i < subsetNames.length; i++) {
        colSpecs[i + dimensionNames.length] =
            new DataColumnSpecCreator(subsetNames[i], IntCell.TYPE).createSpec();
      }
      colSpecs[dimensionNames.length + subsetNames.length] =             
          new DataColumnSpecCreator("SubsetName", StringCell.TYPE).createSpec();

    } else {
      outColumnCount = dimensionNames.length + 1;
      colSpecs = new DataColumnSpec[outColumnCount];
      for (int i = 0; i < displayNames.length; i++) {
        colSpecs[i] = new DataColumnSpecCreator(displayNames[i], DoubleCell.TYPE).createSpec();
      }
    }

    colSpecs[outColumnCount - 1] =
        new DataColumnSpecCreator("Source", StringCell.TYPE).createSpec();

    return new DataTableSpec(colSpecs);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {/* noop */}

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    DataTableSpec inSpec = inSpecs[0];
    if (mSelectedColumn.getColumnName() == DEFAULT_COLUMN) {
      for (int i = 0; i < inSpec.getNumColumns(); i++) {
        inSpec.getColumnSpec(i).getType().equals(FCSFrameFileStoreDataCell.TYPE);
        mSelectedColumn.setSelection(inSpec.getName(), false);
      }
    }
    DataTableSpec outSpec = createSpec(inSpec);
    return new DataTableSpec[] {outSpec};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    mTransform.saveSettingsTo(settings);
    mDownsample.saveSettingsTo(settings);
    mCount.saveSettingsTo(settings);
    mSelectedColumn.saveSettingsTo(settings);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mCount.loadSettingsFrom(settings);
    mSelectedColumn.loadSettingsFrom(settings);
    mTransform.loadSettingsFrom(settings);
    mDownsample.loadSettingsFrom(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    mCount.validateSettings(settings);
    mSelectedColumn.validateSettings(settings);
    mTransform.validateSettings(settings);
    mDownsample.validateSettings(settings);
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
