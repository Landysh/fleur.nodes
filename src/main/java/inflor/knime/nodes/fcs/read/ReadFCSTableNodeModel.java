package inflor.knime.nodes.fcs.read;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import inflor.core.data.FCSFrame;
import inflor.core.fcs.FCSFileReader;
import inflor.core.utils.FCSUtilities;

/**
 * This is the node model implementation for FCSReader (rows). It is designed to use the Inflor
 * FCSFileReader in the context of a KNIME Source node which produces a standard KNIME data table.
 * 
 * @author Aaron Hart
 */
public class ReadFCSTableNodeModel extends NodeModel {

  private static final String ERROR_EXECUTION_FAILED = "Execution Failed while reading data file.";

  private static final String ERROR_CHECK_FILE = "Error while checking file. Check that it exists and is valid.";

  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSTableNodeModel.class);

  // File Location
  static final String KEY_FILE_LOCATION = "File Location";
  static final String DEFAULT_FILE_LOCATION = "NoFile";
  
  // Compensate while reading
  static final String KEY_COMP_ON_READ = "Compensate on read:";

  static final Boolean DEFAULT_COMP_ON_READ = false;

  private static final Object WARN_CHOOSE_A_FILE = "No file selected.";
  private final SettingsModelString mFileLocation =
      new SettingsModelString(KEY_FILE_LOCATION, DEFAULT_FILE_LOCATION);
  
  // Header Only
  static final String KEY_HEADER_ONLY = "Metadata only";

  static final Boolean DEFAULT_HEADER_ONLY = false;

  private final SettingsModelBoolean mHeaderOnly =
      new SettingsModelBoolean(KEY_HEADER_ONLY, DEFAULT_HEADER_ONLY);
  
  

  private int currentKeywordIndex = 0;

  /**
   * Constructor for the node model.
   */
  protected ReadFCSTableNodeModel() {

    // Top port contains header information, bottom, array data
    super(0, 2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    if (mFileLocation.getStringValue().equals(DEFAULT_FILE_LOCATION)) {
      logger.warn(WARN_CHOOSE_A_FILE);
    }
    DataTableSpec[] specs = null;
    try {
      final FCSFileReader reader = new FCSFileReader(mFileLocation.getStringValue());
      final FCSFrame eventsFrame = reader.getFCSFrame();
      specs = createPortSpecs(eventsFrame);
      reader.close();
    } catch (final Exception e) {
      logger.error(ERROR_CHECK_FILE, e);
      throw new InvalidSettingsException(ERROR_CHECK_FILE);
    }
    return specs;
  }

  private DataTableSpec createDataSpec(FCSFrame columnStore) throws InvalidSettingsException {
    List<String> columnNames = columnStore.getDimensionNames();
    DataColumnSpec[] colSpecs = new DataColumnSpec[columnNames.size()];
    for (String columnName : columnNames) {
      int specIndex = FCSUtilities.findParameterNumnberByName(columnStore.getKeywords(), columnName) - 1;
      colSpecs[specIndex] = new DataColumnSpecCreator(columnName, DoubleCell.TYPE).createSpec();
    }
    return new DataTableSpec(colSpecs);
  }

  private DataTableSpec createKeywordSpec() {
    final DataColumnSpec[] colSpecs = new DataColumnSpec[2];
    colSpecs[0] = new DataColumnSpecCreator("keyword", StringCell.TYPE).createSpec();
    colSpecs[1] = new DataColumnSpecCreator("value", StringCell.TYPE).createSpec();
    return new DataTableSpec(colSpecs);
  }

  private DataTableSpec[] createPortSpecs(FCSFrame frame) throws InvalidSettingsException {
    final DataTableSpec[] specs = new DataTableSpec[2];
    specs[0] = createKeywordSpec();
    specs[1] = createDataSpec(frame);
    return specs;
  }

  /**
   * {@inheritDoc}
   * 
   * @throws CanceledExecutionException
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws CanceledExecutionException {

    logger.info("Starting Execution");
    // get table specs
    FCSFileReader reader;
    BufferedDataContainer headerTable = null;
    BufferedDataContainer dataTable = null;

    try {
      reader = new FCSFileReader(mFileLocation.getStringValue());
      Map<String, String> keywords = reader.getHeader();

      FCSFrame columnStore = reader.getFCSFrame();
      DataTableSpec[] tableSpecs = createPortSpecs(columnStore);

      // Read header section"foo"
      headerTable = exec.createDataContainer(tableSpecs[0]);
      readHeader(headerTable, keywords);

      // check in with the boss before we move on.
      exec.checkCanceled();
      exec.setProgress(0.01, "Header read.");

      // Read data section
      dataTable = exec.createDataContainer(tableSpecs[1]);
      if(!mHeaderOnly.getBooleanValue()) {
          reader.initRowReader();
    	  for (Integer j = 0; j < columnStore.getRowCount(); j++) {
              final RowKey rowKey = new RowKey(j.toString());
              DataCell[] dataCells = new DataCell[columnStore.getDimensionCount()];

              final double[] fcsRow = reader.readRow();
              // for each uncomped parameter
              int k = 0;
              while (k < columnStore.getDimensionCount()) {
                // add uncomped data
                dataCells[k] = new DoubleCell(fcsRow[k]);
                k++;
              }
              final DataRow dataRow = new DefaultRow(rowKey, dataCells);
              dataTable.addRowToTable(dataRow);
              if (j % 100 == 0) {
                exec.checkCanceled();
                exec.setProgress(j / (double) columnStore.getRowCount(), j + " rows read.");
              }
            }
      }
      // once we are done, we close the container and return its table
      dataTable.close();
    } catch (final Exception e) {
      logger.error(ERROR_EXECUTION_FAILED, e);
      throw new CanceledExecutionException(ERROR_EXECUTION_FAILED);
    }

    return new BufferedDataTable[] {headerTable.getTable(), dataTable.getTable()};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {
    //TODO
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mFileLocation.loadSettingsFrom(settings);
    mHeaderOnly.loadSettingsFrom(settings);
  }

  private void readHeader(BufferedDataContainer header, Map<String, String> keywords) {
    keywords
      .entrySet()
      .forEach(entry -> writeRow(header, entry));
    header.close();
  }

  private synchronized void writeRow(BufferedDataContainer header, Entry<String, String> entry) {
    String key = entry.getKey();
    String val = entry.getValue();
    final RowKey rowKey = new RowKey("Row " + currentKeywordIndex);
    // the cells of the current row, the types of the cells must match
    // the column spec (see above)
    final DataCell[] keywordCells = new DataCell[2];
    keywordCells[0] = new StringCell(key);
    keywordCells[1] = new StringCell(val);
    final DataRow keywordRow = new DefaultRow(rowKey, keywordCells);
    header.addRowToTable(keywordRow);
    currentKeywordIndex++;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {/**TODO**/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/**TODO**/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    mFileLocation.saveSettingsTo(settings);
    mHeaderOnly.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    mFileLocation.validateSettings(settings);
    mHeaderOnly.validateSettings(settings);
  }
}
