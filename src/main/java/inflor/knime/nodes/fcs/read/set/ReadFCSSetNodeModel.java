package main.java.inflor.knime.nodes.fcs.read.set;

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
import java.util.function.BinaryOperator;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.google.common.primitives.Doubles;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.utils.BitSetUtils;
import main.java.inflor.core.utils.FCSConcatenator;
import main.java.inflor.core.utils.FCSUtilities;
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
  static final String KEY_PATH = "Path";
  static final String DEFAULT_PATH = "None";
  private final SettingsModelString mPath = new SettingsModelString(KEY_PATH, DEFAULT_PATH);

  // Max events per file for preview frame
  static final String KEY_MAX_PER_FILE = "Max per file";
  static final Integer DEFAULT_MAX_PER_FILE = 1000;
  private final SettingsModelIntegerBounded mFileMax =
      new SettingsModelIntegerBounded(KEY_MAX_PER_FILE, DEFAULT_MAX_PER_FILE, 1, Integer.MAX_VALUE);

  // Max events per file for preview frame
  static final String KEY_MAX_PREVIEW_SIZE = "Max preview size";
  static final Integer DEFAULT_MAX_PREVIEW_SIZE = 10000;
  private final SettingsModelIntegerBounded mPreviewMax = new SettingsModelIntegerBounded(
      KEY_MAX_PREVIEW_SIZE, DEFAULT_MAX_PREVIEW_SIZE, 1, Integer.MAX_VALUE);

  private FileStoreFactory fileStoreFactory;

  private int currentFileIndex = 0;
  private int fileCount;
  private String previewFrame;

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
    return new DataTableSpec[] {createSpec(false)};
  }


  private HashMap<String, String> createColumnPropertiesContent(boolean executing) {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    final ArrayList<String> filePaths = getFilePaths(mPath.getStringValue());
    List<Map<String, String>> headers = filePaths.stream().map(FCSFileReader::readHeaderOnly)
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
      
      //Create preview frame
      if (executing){
        createPreviewFrame(filePaths, content);
        content.put(FCSUtilities.PROP_KEY_PREVIEW_FRAME, previewFrame);
      }
    }
    return content;
  }

  private void createPreviewFrame(final ArrayList<String> filePaths,
      final HashMap<String, String> content) {
    int downSize = calculatePreviewSize(content);
    if (previewFrame==null&&downSize>0){
      BinaryOperator<FCSFrame> concater = new FCSConcatenator();
      Optional<FCSFrame> previewMaybe = filePaths
          .parallelStream()
          .map(FCSFileReader::read)
          .map(frame -> downSample(frame, downSize))
          .reduce(concater);
       if (previewMaybe.isPresent()){
         previewFrame = previewMaybe.get().saveAsString();
       }
    }
  }
  
  private FCSFrame downSample(FCSFrame inFrame, int downSize){
    BitSet mask = BitSetUtils.getShuffledMask(inFrame.getRowCount(), downSize);
    return FCSUtilities.filterFrame(mask, inFrame);
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

  private DataTableSpec createSpec(boolean executing) {
    DataColumnSpecCreator creator =
        new DataColumnSpecCreator("FCS Frame", FCSFrameFileStoreDataCell.TYPE);
    // Create properties
    HashMap<String, String> content = createColumnPropertiesContent(executing);
    DataColumnProperties properties = new DataColumnProperties(content);
    creator.setProperties(properties);
    // Create spec
    DataColumnSpec dcs = creator.createSpec();
    DataColumnSpec[] colSpecs = new DataColumnSpec[] {dcs};
    return new DataTableSpec(colSpecs);
  }

  // dont listen to sonar, used in createColumnPropertiesContent
  private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
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
    final DataTableSpec outSpec = createSpec(false);//TODO
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

  private synchronized void addRow(FCSFrame dataFrame, BufferedDataContainer container,
      ExecutionContext exec) {
	
	//create the row
    final RowKey key = new RowKey("Row " + currentFileIndex);
    final String fsName = currentFileIndex + "ColumnStore.fs";
    FileStore fileStore;
    try {
      fileStore = fileStoreFactory.createFileStore(fsName);
      final FCSFrameFileStoreDataCell fileCell =
          new FCSFrameFileStoreDataCell(fileStore, dataFrame);
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

  private int calculatePreviewSize(HashMap<String, String> content) {
    //Count the files
    String fileNames = content.get("$FIL");
    int numFiles = fileNames.split(NodeUtilities.DELIMITER_REGEX).length;
    //minimum events per events per file
    String countString = content.get(FCSUtilities.FCSKEY_EVENT_COUNT);
    String[] countStrings = countString.split(NodeUtilities.DELIMITER_REGEX);
    double[] counts = new double[countStrings.length];
    for (int i=0;i<countStrings.length;i++){
      counts[i] = Double.parseDouble(countStrings[i]);
    }
    int minCount = (int) Doubles.min(counts);
    int perFileCount;
    if (minCount >= mFileMax.getIntValue()){
      perFileCount = mFileMax.getIntValue();
    } else {
      perFileCount = minCount;
      logger.warn("1 or more files do not have enough events for specified sample size.  Using: " + minCount + " instead.");
    }
    //Check that we don't exceed a "reasonable" number of events.
    if (perFileCount*fileCount>mPreviewMax.getIntValue()){
      perFileCount = mFileMax.getIntValue()/numFiles;
      logger.warn("The resulting preview frame is too large. Using: " + perFileCount + " events per file instead.");
    } 
    return perFileCount * numFiles;
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
      throws IOException, CanceledExecutionException {/* TODO */}

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
      throws IOException, CanceledExecutionException {/* TODO */}

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
