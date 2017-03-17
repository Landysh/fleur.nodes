package inflor.knime.nodes.utility.extract.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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

import inflor.core.data.FCSFrame;
import inflor.core.data.Subset;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.MatrixUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;


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

  private int rowIndex;
  private DataTableSpec outputSpec;
  private TransformSet transformSet;

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

    // Read the transform map.
    final String columnName = mSelectedColumn.getColumnName();
    DataTableSpec spec = inData[0].getSpec();
    DataColumnProperties props = inData[0].getSpec().getColumnSpec(columnName).getProperties();
    
    if (props.containsProperty(NodeUtilities.KEY_TRANSFORM_MAP)) {
      String transformString = props.getProperty(NodeUtilities.KEY_TRANSFORM_MAP);
      transformSet =
          TransformSet.loadFromProtoString(transformString);
    } else {
      throw new CanceledExecutionException("Unable to parse transform map");
    }

    // list the cells.
    ExecutionContext listExec = exec.createSubExecutionContext(0.1);
    ArrayList<FCSFrameFileStoreDataCell> dataSet = new ArrayList<>();
    int fileCount = 0;
    for (DataRow inRow : inData[0]) {
      FCSFrameFileStoreDataCell cell = (FCSFrameFileStoreDataCell) inRow.getCell(fileCount);
      dataSet.add(cell);
      // update the progress bar
      listExec.checkCanceled();
      listExec.setProgress((double) fileCount / inData[0].size());
      listExec.setMessage("Reading: " + cell.getFCSFrameMetadata().getDisplayName());
    }
    
    BufferedDataContainer container = exec.createDataContainer(outputSpec);
    
    rowIndex = 0;
    dataSet
      .parallelStream()
      .map(cell -> cell.getFCSFrameValue())
      .forEach(df -> this.writeFrame(df,container,exec));
    
    container.close();
    BufferedDataTable out = container.getTable();
    return new BufferedDataTable[] {out};
  }

  private void writeFrame(FCSFrame df, BufferedDataContainer container, ExecutionContext exec) {
    double[][] data = df.getMatrix(df.getDimensionNames());
    if (mTransform.getBooleanValue()){
    	List<String> names = df.getDimensionNames();
      FCSUtilities.transformMatrix(names, transformSet, data);
    }
    double[][] rowData = MatrixUtilities.transpose(data);
    List<Subset> subsets = df.getSubsets();
    for (int i=0;i<rowData.length;i++){
      RowKey rowKey = new RowKey(df.toString() + i);//TODO maybe not unique. 
      DataCell[] dataCells = new DataCell[df.getDimensionCount()+subsets.size()+2];
      for (int j=0;j<df.getDimensionCount();j++){
        dataCells[j] = new DoubleCell(rowData[i][j]);
      }
      
      String subsetLabel = "Ungated";
      for (int k=0;k<subsets.size();k++){
        if (subsets.get(k).getMembers().get(i)){
          subsetLabel = subsetLabel + File.separatorChar + subsets.get(k).getLabel();
        }
        dataCells[df.getDimensionCount() + k] = subsets.get(k).getMembers().get(i) ? new StringCell(subsets.get(k).getLabel()): new StringCell("");
      }
      
      dataCells[dataCells.length-2] = new StringCell(subsetLabel);
      dataCells[dataCells.length-1] = new StringCell(df.getDisplayName()); 
      DataRow row = new DefaultRow(rowKey, dataCells);
      
      synchronized (container) {
        rowIndex++;
        container.addRowToTable(row);
      }

    }
  }

  private DataTableSpec createSpec(DataTableSpec inSpec) throws InvalidSettingsException {
    String columnName = mSelectedColumn.getColumnName();
    DataColumnProperties properties = inSpec.getColumnSpec(columnName).getProperties();
    String rawDimensionNames = properties.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    String rawDisplayNames = properties.getProperty(NodeUtilities.DISPLAY_NAMES_KEY);
    if (rawDimensionNames==null||rawDisplayNames==null){
      throw new InvalidSettingsException("No ouput dimensions available. Have you executed your file reader?");
    }
    String[] dimensionNames = rawDimensionNames.split(NodeUtilities.DELIMITER_REGEX);
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
            new DataColumnSpecCreator(subsetNames[i], StringCell.TYPE).createSpec();
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
    mSelectedColumn.saveSettingsTo(settings);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mSelectedColumn.loadSettingsFrom(settings);
    mTransform.loadSettingsFrom(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    mSelectedColumn.validateSettings(settings);
    mTransform.validateSettings(settings);
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
