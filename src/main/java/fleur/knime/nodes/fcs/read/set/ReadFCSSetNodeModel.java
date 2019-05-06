package fleur.knime.nodes.fcs.read.set;

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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import inflor.core.compensation.SpilloverCompensator;
import fleur.core.data.FCSDimension;
import fleur.core.data.FCSFrame;
import fleur.core.fcs.FCSFileReader;
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

  private static final String FCS_COLUMN_NAME = "FCS Frame";
  private static final String SOURCE_COLUMN_NAME = "Source";
  private static final String KW_NAME_COLUMN_NAME = "Name";
  private static final String KW_VALUE_COLUMN_NAME = "Value";

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSSetNodeModel.class);
  
  // Port Types
  protected static final PortType IN_PORT_TYPE_0 = PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class, true);
  protected static final PortType OUT_PORT_TYPE_0 = PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class, false);
  protected static final PortType OUT_PORT_TYPE_1 = PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class, false);
  
  // Default Preview Frame Settings.
  // The maximum size of the preview frame (in measurements eg. 100kevents *
  // 10 dimensions)

  private FileStoreFactory fileStoreFactory;

  private int currentFileIndex = 0;
  private int fileCount;
  private FCSFrame previewFrame;
  
  ReadFCSSetNodeModel() {
    
    super(new PortType[] {IN_PORT_TYPE_0}, new PortType[] {OUT_PORT_TYPE_0, OUT_PORT_TYPE_1});
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
          
    DataColumnSpecCreator creator0 = new DataColumnSpecCreator(FCS_COLUMN_NAME, FCSFrameFileStoreDataCell.TYPE);
    DataColumnSpec[] colSpecs0 = new DataColumnSpec[] {creator0.createSpec()};
    DataTableSpec spec0 = new DataTableSpec(colSpecs0);
       
    DataColumnSpecCreator source = new DataColumnSpecCreator(SOURCE_COLUMN_NAME, StringCell.TYPE);    
    DataColumnSpecCreator name = new DataColumnSpecCreator(KW_NAME_COLUMN_NAME, StringCell.TYPE);
    DataColumnSpecCreator value = new DataColumnSpecCreator(KW_VALUE_COLUMN_NAME, StringCell.TYPE);
    
    DataColumnSpec[] colSpecs1 = new DataColumnSpec[] {source.createSpec(), name.createSpec(), value.createSpec()};
    
    DataTableSpec spec1 = new DataTableSpec(colSpecs1);

    return new DataTableSpec[] {spec0, spec1};
  }

  static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    // http://stackoverflow.com/questions/23699371/java-8-distinct-by-property
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;

  }

  private HashMap<String, String> createColumnPropertiesContent(List<String> paths) {
    /**
     * Creates column properties for an FCS Set by looking all of the headers and setting shared
     * keyword values.
     */
    //final ArrayList<String> filePaths = getFilePaths(mPath.getStringValue());
    List<Map<String, String>> headers = paths.parallelStream().map(FCSFileReader::readHeaderOnly)
        .filter(map -> !map.isEmpty()).collect(Collectors.toList());

    final HashMap<String, String> content = new HashMap<>();
    // Merge all keywords.
    headers.forEach(map -> map.entrySet().forEach(entry -> updateContent(content, entry)));

    // Collect all dimensions for experiment in one Hashset.
    Optional<TreeSet<FCSDimension>> optionalDimensions = paths.stream()
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

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
    currentFileIndex = 0;
    logger.info("Beginning Execution.");
    fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    // Create the output spec and data container.
    DataTableSpec[] inSpecs;
    Boolean readFromColumn = ReadFCSSetSettings.getMMode().equals(ReadFCSSetSettings.ReaderModes.Column.toString());
    if (readFromColumn) {
      inSpecs = new DataTableSpec[] {inData[0].getSpec()};
    } else {
      inSpecs = new DataTableSpec[] {null};
    }
    final DataTableSpec[] outSpecs = configure(inSpecs);
    final BufferedDataContainer container0 = exec.createDataContainer(outSpecs[0]);
    final BufferedDataContainer container1 = exec.createDataContainer(outSpecs[1]);

    List<String> filePaths = new ArrayList<String>();
    String mPath = ReadFCSSetSettings.getPathValue();
    try {
      if (readFromColumn) {
        filePaths = readInputPaths(inData[0]);
      } else if (!readFromColumn) {
        filePaths = getFilePaths(mPath);
      } else {
        throw new NullPointerException("Unexpected Error"); 
      }
    } catch (Exception e) {
    	throw new RuntimeException("Unable to read dir: " + mPath );
    }
    fileCount = filePaths.size();
    exec.checkCanceled();
    try {
      filePaths
        .parallelStream()
        .map(FCSFileReader::read)
        .forEach(fcsFrame -> addRow(fcsFrame, container0, container1, exec));
    } catch (NullPointerException e){
      logger.error("Execution Failed", e);
      throw new RuntimeException("Execution failed.");
    }
    exec.checkCanceled();
    exec.setMessage("Finished reading files, creating summary frame.");
    // once we are done, we close the container and return its table
    container0.close();
    container1.close();
    BufferedDataTable inTable = container0.getTable();

    String columnName = FCS_COLUMN_NAME;
    String key = NodeUtilities.PREVIEW_FRAME_KEY;
    previewFrame.setDisplayName(NodeUtilities.PREVIEW_FRAME_KEY);
    String value = previewFrame.saveAsString();
    
    // Create properties
    HashMap<String, String> content = createColumnPropertiesContent(filePaths);
    content.put(key, value);
    
    BufferedDataTable finalTable =
        NodeUtilities.addPropertyToColumn(exec, inTable, columnName, content);
    return new BufferedDataTable[] {finalTable, container1.getTable()};
  }

  private List<String> readInputPaths(BufferedDataTable inData) {
    String mColumn = ReadFCSSetSettings.getColumnValue();
    List<String> paths = new ArrayList<String>();      
    if (inData.getDataTableSpec().containsName(mColumn)) {
      int colIndex = inData.getDataTableSpec().findColumnIndex(mColumn);
      inData.iterator()
        .forEachRemaining(row -> paths.add(row.getCell(colIndex).toString()));
    } else {
      throw new RuntimeException("Selected Column not found in input table.");
    }
    return paths;
  }

  private synchronized void addRow(FCSFrame df, BufferedDataContainer container,
      BufferedDataContainer container1, ExecutionContext exec) {
    //Compensate from the header. 
    Boolean mComp = ReadFCSSetSettings.getMComp();
    if (mComp) {
      SpilloverCompensator sc = new SpilloverCompensator(df.getKeywords());
      df = sc.compensateFCSFrame(df, true);
    }
    
    String source = df.getDisplayName();
    df.getKeywords().entrySet().forEach(entry -> addMetadata(source, entry, container1, exec));
    
    // Create Preview frame.
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
      container.addRowToTable(new DefaultRow(key, new DataCell[] {fileCell}));
      try {
        exec.checkCanceled();
      } catch (CanceledExecutionException e) {
        // Unchecked exception as this is called from a stream.
        throw new NullPointerException("Execution cancelled");
      }
      exec.setProgress(currentFileIndex / (double) fileCount,
          "Reading file " + (currentFileIndex + 1) + " of: " + fileCount);
      currentFileIndex++;
    } catch (IOException e) {
      logger.error("Row not added for frame: " + currentFileIndex, e);
    }
  }

  private void addMetadata(String source, Entry<String, String> entry, BufferedDataContainer container1, ExecutionContext exec) {
    DataCell[] cells = new DataCell[] {new StringCell(source),new StringCell(entry.getKey()), new StringCell(entry.getValue())};
    DataRow row = new DefaultRow(UUID.randomUUID().toString(), cells);
    container1.addRowToTable(row);
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
   ReadFCSSetSettings.load(settings);
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
     ReadFCSSetSettings.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    ReadFCSSetSettings.validate(settings);

  }
}