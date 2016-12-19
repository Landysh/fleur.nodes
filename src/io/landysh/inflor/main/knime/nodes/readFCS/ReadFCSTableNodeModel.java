package io.landysh.inflor.main.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.utils.FCSUtilities;

/**
 * This is the node model implementation for FCSReader (rows). It is designed to use the Inflor
 * FCSFileReader in the context of a KNIME Source node which produces a standard KNIME data table.
 * 
 * @author Aaron Hart
 */
public class ReadFCSTableNodeModel extends NodeModel {

  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSTableNodeModel.class);

  // File Location
  static final String CFGKEY_FileLocation = "File Location";
  static final String DEFAULT_FileLocation = null;
  // Compensate while reading
  static final String KEY_Compensate = "Compensate on read:";

  static final Boolean DEFAULT_Compensate = false;
  private final SettingsModelString m_FileLocation =
      new SettingsModelString(CFGKEY_FileLocation, DEFAULT_FileLocation);

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
    if (m_FileLocation.getStringValue() == null) {
      throw new InvalidSettingsException(
          "There is no file to read. Please select a valid FCS file.");
    }
    DataTableSpec[] specs = null;
    try {
      final FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
      final FCSFrame eventsFrame = FCSReader.getColumnStore();
      specs = createPortSpecs(eventsFrame);
      FCSReader.close();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException(
          "Error while checking file. Check that it exists and is valid.");
    }
    return specs;
  }

  private DataTableSpec createDataSpec(FCSFrame columnStore) throws InvalidSettingsException {
    List<String> columnNames = columnStore.getColumnNames();
    DataColumnSpec[] colSpecs = new DataColumnSpec[columnNames.size()];
    for (String columnName : columnNames) {
      int specIndex = FCSUtilities.findParameterNumnberByName(columnStore.getKeywords(), columnName) - 1;
      colSpecs[specIndex] = new DataColumnSpecCreator(columnName, DoubleCell.TYPE).createSpec();
    }
    final DataTableSpec dataSpec = new DataTableSpec(colSpecs);
    return dataSpec;
  }

  private DataTableSpec createKeywordSpec() {
    final DataColumnSpec[] colSpecs = new DataColumnSpec[2];
    colSpecs[0] = new DataColumnSpecCreator("keyword", StringCell.TYPE).createSpec();
    colSpecs[1] = new DataColumnSpecCreator("value", StringCell.TYPE).createSpec();

    final DataTableSpec headerSpec = new DataTableSpec(colSpecs);
    return headerSpec;
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
    FCSFileReader FCSReader;
    BufferedDataContainer headerTable = null;
    BufferedDataContainer dataTable = null;

    try {
      FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
      Map<String, String> keywords = FCSReader.getHeader();

      FCSFrame columnStore = FCSReader.getColumnStore();
      DataTableSpec[] tableSpecs = createPortSpecs(columnStore);

      // Read header section
      headerTable = exec.createDataContainer(tableSpecs[0]);
      readHeader(headerTable, keywords);

      // check in with the boss before we move on.
      exec.checkCanceled();
      exec.setProgress(0.01, "Header read.");

      // Read data section
      dataTable = exec.createDataContainer(tableSpecs[1]);
      FCSReader.initRowReader();
      for (Integer j = 0; j < columnStore.getRowCount(); j++) {
        final RowKey rowKey = new RowKey(j.toString());
        DataCell[] dataCells = new DataCell[columnStore.getColumnCount()];

        final double[] FCSRow = FCSReader.readRow();
        // for each uncomped parameter
        int k = 0;
        while (k < columnStore.getColumnCount()) {
          // add uncomped data
          dataCells[k] = new DoubleCell(FCSRow[k]);
          k++;
        }
        final DataRow dataRow = new DefaultRow(rowKey, dataCells);
        dataTable.addRowToTable(dataRow);
        if (j % 100 == 0) {
          exec.checkCanceled();
          exec.setProgress(j / (double) columnStore.getRowCount(), j + " rows read.");
        }
      }
      // once we are done, we close the container and return its table
      dataTable.close();
    } catch (final Exception e) {
      exec.setMessage("Execution Failed while reading data file.");
      e.printStackTrace();
      throw new CanceledExecutionException("Execution Failed while reading data file.");
    }

    return new BufferedDataTable[] {headerTable.getTable(), dataTable.getTable()};
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
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    m_FileLocation.loadSettingsFrom(settings);
  }

  private void readHeader(BufferedDataContainer header, Map<String, String> keywords) {
    keywords.entrySet().forEach(entry -> writeRow(header, entry));
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
  protected void reset() {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    m_FileLocation.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

    m_FileLocation.validateSettings(settings);
  }
}
