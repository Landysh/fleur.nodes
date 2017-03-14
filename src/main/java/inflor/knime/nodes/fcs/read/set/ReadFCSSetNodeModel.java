package inflor.knime.nodes.fcs.read.set;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.fcs.FCSFileReader;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSConcatenator;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;
import inflor.knime.data.type.cell.fcs.FCSFrameMetaData;

/**
 * This is the model implementation of ReadFCSSet.
 * 
 *
 * @author Aaron Hart
 */
public class ReadFCSSetNodeModel extends NodeModel {

  private static final String FCS_FRAME_COLUMN_NAME = "FCS Frame";

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSSetNodeModel.class);

  // Folder containing FCS Files.
  static final String KEY_PATH = "Path";
  static final String DEFAULT_PATH = "None";
  private final SettingsModelString mPath = new SettingsModelString(KEY_PATH, DEFAULT_PATH);

  // Default Preview Frame Settings.
  // The maximum size of the preview frame (in measurements eg. 100kevents *
  // 10 dimensions)

  private FileStoreFactory fileStoreFactory;

  private int currentFileIndex = 0;
  private int fileCount;
  private FCSFrame previewFrame;

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

    if (!mPath.getStringValue().equals(DEFAULT_PATH)) {
      return new DataTableSpec[] {createSpec()};
    } else {
      throw new InvalidSettingsException("Please select a file path.");
    }

  }

  private HashMap<String, String> createColumnPropertiesContent() {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final ArrayList<String> filePaths = getFilePaths(mPath.getStringValue());
    List<Map<String, String>> headers = filePaths.parallelStream().map(FCSFileReader::readHeaderOnly)
        .filter(map -> !map.isEmpty()).collect(Collectors.toList());

    final HashMap<String, String> content = new HashMap<>();
    // Merge all keywords.
    headers.forEach(map -> map.entrySet().forEach(entry -> updateContent(content, entry)));

    // Collect all dimensions for experiment in one Hashset.
    Optional<TreeSet<FCSDimension>> optionalDimensions = filePaths.stream()
        .map(FCSFileReader::readNoData).map(FCSFrame::getData).reduce(this::merge);

    if (optionalDimensions.isPresent()) {
      // Add dimension names string
      ArrayList<String> shortNames = optionalDimensions.get().stream().sequential()
          .filter(distinctByKey(FCSDimension::getShortName)).map(FCSDimension::getShortName)
          .collect(Collectors.toCollection(ArrayList::new));

      String dimensionNames = String.join(NodeUtilities.DELIMITER, shortNames);
      logger.info(dimensionNames);
      content.put(NodeUtilities.DIMENSION_NAMES_KEY, dimensionNames);

      // create and add display names string
      ArrayList<String> displayNames = optionalDimensions.get().stream().sequential()
          .filter(distinctByKey(FCSDimension::getShortName)).map(FCSDimension::getDisplayName)
          .collect(Collectors.toCollection(ArrayList::new));

      String displayNamesString = String.join(NodeUtilities.DELIMITER, displayNames);
      logger.info(displayNamesString);
      content.put(NodeUtilities.DISPLAY_NAMES_KEY, displayNamesString);
    }
    return content;
  }

  private FCSFrame downSample(FCSFrame inFrame) {
    int downSize = FCSUtilities.DEFAULT_MAX_SUMMARY_FRAME_VALUES / fileCount / inFrame.getDimensionCount();
    if (downSize < inFrame.getRowCount()) {
      BitSet mask = BitSetUtils.getShuffledMask(inFrame.getRowCount(), downSize);
      return FCSUtilities.filterFrame(mask, inFrame);
    } else {
      return inFrame;
    }

  }

  private TreeSet<FCSDimension> merge(TreeSet<FCSDimension> a, TreeSet<FCSDimension> b) {
    a.addAll(b);
    return a;
  }

  private void updateContent(HashMap<String, String> content, Entry<String, String> entry) {
    if (content.containsKey(entry.getKey())) {
      String currentValue = content.get(entry.getKey());
      String[] cvString = currentValue.split(NodeUtilities.DELIMITER_REGEX);
      if (!Arrays.asList(cvString).contains(entry.getValue())) {
        currentValue = currentValue + NodeUtilities.DELIMITER + entry.getValue();
        content.put(entry.getKey(), currentValue);
      }
    } else {
      content.put(entry.getKey(), entry.getValue());
    }
  }

  private DataTableSpec createSpec() {
    DataColumnSpecCreator creator =
        new DataColumnSpecCreator(FCS_FRAME_COLUMN_NAME, FCSFrameFileStoreDataCell.TYPE);
    // Create properties
    HashMap<String, String> content = createColumnPropertiesContent();
    DataColumnProperties properties = new DataColumnProperties(content);
    creator.setProperties(properties);
    // Create spec
    DataColumnSpec dcs = creator.createSpec();
    DataColumnSpec[] colSpecs = new DataColumnSpec[] {dcs};
    return new DataTableSpec(colSpecs);
  }

  static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    // http://stackoverflow.com/questions/23699371/java-8-distinct-by-property
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
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
    try {
      filePaths
        .parallelStream()
        .map(FCSFileReader::read)
        .forEach(columnStore -> addRow(columnStore, container, exec));
    } catch (NullPointerException e){
      throw new CanceledExecutionException("Execution cancelled.");
    }
    exec.checkCanceled();
    exec.setMessage("Finished reading files, creating summary frame.");
    // once we are done, we close the container and return its table
    container.close();
    BufferedDataTable inTable = container.getTable();

    String columnName = FCS_FRAME_COLUMN_NAME;
    String key = NodeUtilities.PREVIEW_FRAME_KEY;
    previewFrame.setDisplayName(NodeUtilities.PREVIEW_FRAME_KEY);
    String value = previewFrame.saveAsString();

    BufferedDataTable finalTable =
        NodeUtilities.addPropertyToColumn(exec, inTable, columnName, key, value);
    return new BufferedDataTable[] {finalTable};
  }



  private synchronized void addRow(FCSFrame df, BufferedDataContainer container,
      ExecutionContext exec) {

    FCSConcatenator concatr = new FCSConcatenator();
    FCSFrame f1 = downSample(df);
    if (previewFrame == null) {
      previewFrame = f1;
    } else {
      previewFrame = concatr.apply(previewFrame, f1);
    }

    // create the row
    final RowKey key = new RowKey("Row " + currentFileIndex);
    try {
      FileStore fs = fileStoreFactory.createFileStore(df.toString() + "." + df.getID());
      int sizeSaved = NodeUtilities.writeFrameToFilestore(df, fs);
      FCSFrameMetaData metaData = new FCSFrameMetaData(df, sizeSaved);
      final FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fs, metaData);
      final DataCell[] cells = new DataCell[] {fileCell};

      final DataRow row = new DefaultRow(key, cells);
      container.addRowToTable(row);
      try {
        exec.checkCanceled();
      } catch (CanceledExecutionException e) {
        throw new NullPointerException("Execution cancelled");
      }
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
      throws IOException, CanceledExecutionException {/* noop */}

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
  protected void reset() {
    previewFrame = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {
    /* noop */}

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