package io.landysh.inflor.main.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.utils.FCSUtilities;
import io.landysh.inflor.main.knime.core.NodeUtilities;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of ReadFCSSet.
 * 
 *
 * @author Landysh Co.
 */
public class ReadFCSSetNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSSetNodeModel.class);

  // Folder containing FCS Files.
  static final String CFGKEY_PATH = "Path";
  static final String DEFAULT_PATH = null;

  private final SettingsModelString m_path = new SettingsModelString(CFGKEY_PATH, DEFAULT_PATH);

  private FileStoreFactory fileStoreFactory;

  private int currentFileIndex = 0;
  private int fileCount;

  /**
   * Constructor for the node model.
   */
  protected ReadFCSSetNodeModel() {
    super(0, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    DataTableSpec spec;
    try {
      spec = createSpec();
    } catch (final Exception e) {
      final InvalidSettingsException ise =
          new InvalidSettingsException("Unable to read headers of 1 or more FCS Files.");
      ise.printStackTrace();
      throw ise;
    }
    return new DataTableSpec[] {spec};
  }

  private HashMap<String, String> createColumnPropertiesContent() {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final ArrayList<String> filePaths = getFilePaths(m_path.getStringValue());
    List<HashMap<String, String>> headers = filePaths
        .stream()
        .map(path -> FCSFileReader.readHeaderOnly(path))
        .collect(Collectors.toList());

    final HashMap<String, String> content = new HashMap<String, String>();
    HashSet<String> shortNames = new HashSet<>();

    // Merge all keywords.
    headers
        .forEach(map -> map.entrySet()
            .forEach(entry -> updateContent(content, entry, shortNames)));


    // Collect all parameter for experiment in one Hashset.
    headers
      .stream()
      .map(header -> FCSUtilities.parseDimensionList(header))
      .forEach(dimensionList -> updateShortNames(dimensionList, shortNames));
    
    String dimensionNames = "";
    for (String name : shortNames) {
      dimensionNames = dimensionNames + name + NodeUtilities.DELIMITER;
    }
    dimensionNames = dimensionNames.substring(0, dimensionNames.length() - NodeUtilities.DELIMITER.length());
    System.out.println(dimensionNames);
    content.put(NodeUtilities.DIMENSION_NAMES_KEY, dimensionNames);

    return content;
  }

  private void updateShortNames(String[] newDimensions, HashSet<String> allDimensions) {
    List<String> ndl = Arrays.asList(newDimensions);
    allDimensions.addAll(ndl);
  }

  private void updateContent(HashMap<String, String> content, Entry<String, String> entry,
      HashSet<String> shortNames) {
    if (content.containsKey(entry.getKey())) {
      String currentValue = content.get(entry.getKey());
      currentValue = currentValue + "||" + entry.getValue();
    } else {
      content.put(entry.getKey(), entry.getValue());
    }
  }

  private DataColumnSpec createFCSColumnSpec() {
    DataColumnSpecCreator creator =
        new DataColumnSpecCreator("FCS Frame", FCSFrameFileStoreDataCell.TYPE);
    // Create properties
    HashMap<String, String> content = createColumnPropertiesContent();
    DataColumnProperties properties = new DataColumnProperties(content);
    creator.setProperties(properties);
    // Create spec
    DataColumnSpec dcs = creator.createSpec();
    return dcs;
  }

  private DataTableSpec createSpec() throws Exception {
    DataColumnSpec[] colSpecs = new DataColumnSpec[] {createFCSColumnSpec()};
    DataTableSpec tableSpec = new DataTableSpec(colSpecs);
    return tableSpec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {
    currentFileIndex = 0;
    logger.info("Beginning Execution.");
    fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    // Create the output spec and data container.
    final DataTableSpec outSpec = createSpec();
    final BufferedDataContainer container = exec.createDataContainer(outSpec);
    final ArrayList<String> filePaths = getFilePaths(m_path.getStringValue());
    fileCount = filePaths.size();
    exec.checkCanceled();
    filePaths
      .parallelStream()
      .map(path -> FCSFileReader.read(path))
      .forEach(columnStore -> addRow(columnStore, container, exec));
    exec.checkCanceled();

    // once we are done, we close the container and return its table
    container.close();
    final BufferedDataTable out = container.getTable();
    return new BufferedDataTable[] {out};
  }

  private synchronized void addRow(FCSFrame columnStore, BufferedDataContainer container,
      ExecutionContext exec) {
    final RowKey key = new RowKey("Row " + currentFileIndex);
    final String fsName = currentFileIndex + "ColumnStore.fs";
    FileStore fileStore;
    try {
      fileStore = fileStoreFactory.createFileStore(fsName);
      final FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fileStore, columnStore);
      final DataCell[] cells = new DataCell[] {fileCell};

      final DataRow row = new DefaultRow(key, cells);
      container.addRowToTable(row);

      exec.setProgress(currentFileIndex / (double) fileCount,
          "Reading file " + (currentFileIndex + 1) + " of: " + fileCount);
      currentFileIndex++;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private ArrayList<String> getFilePaths(String dirPath) {
    /**
     * Returns a list of valid FCS Files from the chose directory.
     */
    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<String>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath) == true) {
        validFiles.add(filePath);
      } else if (file.isDirectory()) {
        System.out.println("Directory " + file.getName());
      }
    }
    return validFiles;
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
    m_path.loadSettingsFrom(settings);
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
    m_path.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    m_path.validateSettings(settings);
  }
}
