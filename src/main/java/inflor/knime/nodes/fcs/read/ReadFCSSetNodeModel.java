package main.java.inflor.knime.nodes.fcs.read;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.knime.core.NodeUtilities;
import main.java.inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of ReadFCSSet.
 * 
 *
 * @author Aaron Hart
 */
public class ReadFCSSetNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSSetNodeModel.class);

  // Folder containing FCS Files.
  static final String CFGKEY_PATH = "Path";
  static final String DEFAULT_PATH = "None";

  private final SettingsModelString mPath = new SettingsModelString(CFGKEY_PATH, DEFAULT_PATH);

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
      return new DataTableSpec[] {createSpec()};
  }

   
  private HashMap<String, String> createColumnPropertiesContent() {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final ArrayList<String> filePaths = getFilePaths(mPath.getStringValue());
    List<Map<String, String>> headers = filePaths
        .stream()
        .map(FCSFileReader::readHeaderOnly)
        .filter(map -> !map.isEmpty())
        .collect(Collectors.toList());
    
    
    final HashMap<String, String> content = new HashMap<>();
    // Merge all keywords.
    headers
        .forEach(map -> map.entrySet()
            .forEach(entry -> updateContent(content, entry)));

    // Collect all parameter for experiment in one Hashset.
    Optional<TreeSet<FCSDimension>> opt = filePaths
      .stream()
      .map(FCSFileReader::readNoData)
      .map(FCSFrame::getData)
      .reduce(this::merge);
    
    
    if (opt.isPresent()){
      TreeSet<FCSDimension> set = opt.get();
      ArrayList<String> shortNames = set.stream()
      .sequential()
      .filter(distinctByKey(FCSDimension::getShortName))
      .map(FCSDimension::getShortName)
      .collect(Collectors.toCollection(ArrayList::new));
      
      String dimensionNames = String.join(NodeUtilities.DELIMITER, shortNames);
      logger.info(dimensionNames);
      content.put(NodeUtilities.DIMENSION_NAMES_KEY, dimensionNames);
    }
    return content;
  }

  private TreeSet<FCSDimension> merge(TreeSet<FCSDimension> a, TreeSet<FCSDimension> b) {
    a.addAll(b);
    return a;
  }

  private void updateContent(HashMap<String, String> content, Entry<String, String> entry) {
    if (content.containsKey(entry.getKey())) {
      String currentValue = content.get(entry.getKey());
      String[] cvString = currentValue.split(NodeUtilities.DELIMITER_REGEX);
      if (!Arrays.asList(cvString).contains(entry.getValue())){
        currentValue = currentValue + NodeUtilities.DELIMITER + entry.getValue();
        content.put(entry.getKey(), currentValue);
      }
    } else {
      content.put(entry.getKey(), entry.getValue());
    }
  }

  private DataTableSpec createSpec() {
    DataColumnSpecCreator creator =
        new DataColumnSpecCreator("FCS Frame", FCSFrameFileStoreDataCell.TYPE);
    // Create properties
    HashMap<String, String> content = createColumnPropertiesContent();
    DataColumnProperties properties = new DataColumnProperties(content);
    creator.setProperties(properties);
    // Create spec
    DataColumnSpec dcs = creator.createSpec();
    DataColumnSpec[] colSpecs = new DataColumnSpec[] {dcs};
    return new DataTableSpec(colSpecs);
  }
  //dont listen to sonar, used in createColumnPropertiesContent
  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    //http://stackoverflow.com/questions/23699371/java-8-distinct-by-property
    Map<Object,Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
    final ArrayList<String> filePaths = getFilePaths(mPath.getStringValue());
    fileCount = filePaths.size();
    exec.checkCanceled();
    filePaths
      .parallelStream()
      .map(FCSFileReader::read)
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
      logger.error("Row not added for frame: " + currentFileIndex, e);
    }
  }

  private ArrayList<String> getFilePaths(String dirPath) {
    /**
     * Returns a list of valid FCS Files from the chose directory.
     */
    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath)) {
        validFiles.add(filePath);
      } else if (file.isDirectory()) {
        logger.info("Directory " + file.getName());
      }
    }
    return validFiles;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*TODO*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mPath.loadSettingsFrom(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {/*TODO*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*TODO*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    mPath.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    mPath.validateSettings(settings);
  }
}
