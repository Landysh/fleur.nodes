package inflor.knime.nodes.gating;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
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

import inflor.core.data.FCSFrame;
import inflor.core.data.Subset;
import inflor.core.gates.AbstractGate;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of CreateGates.
 * 
 *
 * @author
 */
public class CreateGatesNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(CreateGatesNodeModel.class);

  CreateGatesNodeSettings modelSettings;

private TransformSet transformSet;

  /**
   * Constructor for the node model.
   */
  protected CreateGatesNodeModel() {
    super(1, 1);
    modelSettings = new CreateGatesNodeSettings();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    return createSpecs(inSpecs[0]);
  }

  private DataTableSpec[] createSpecs(DataTableSpec inSpec) throws InvalidSettingsException {
    if (CreateGatesNodeSettings.DEFAULT_SELECTED_COLUMN.equals(modelSettings.getSelectedColumn())){
      throw new InvalidSettingsException("Please select a column containing FCS Frame data");
    }
    DataTableSpecCreator creator = new DataTableSpecCreator(inSpec);
    DataColumnProperties properties = inSpec.getColumnSpec(modelSettings.getSelectedColumn()).getProperties();
    List<String> subsetNames = modelSettings
        .getNodes()
        .values()
        .stream()
        .filter(node -> node instanceof AbstractGate)
        .map(node -> (AbstractGate) node)
        .map(AbstractGate::getLabel)
        .collect(Collectors.toList());
    String subSetNamesString = String.join(NodeUtilities.DELIMITER, subsetNames);
    Map<String, String> newProperties = new HashMap<>();
    newProperties.put(NodeUtilities.SUBSET_NAMES_KEY, subSetNamesString);
    DataColumnProperties newProps = properties.cloneAndOverwrite(newProperties);    
    DataColumnSpec cspec = inSpec.getColumnSpec(modelSettings.getSelectedColumn());
    DataColumnSpecCreator colCreartor = new DataColumnSpecCreator(cspec);
    colCreartor.setProperties(newProps);
    creator.replaceColumn(inSpec.findColumnIndex(modelSettings.getSelectedColumn()), colCreartor.createSpec());
    DataTableSpec outSpec = creator.createSpec();
    return new DataTableSpec[] {outSpec};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {

    logger.info("Executing: Create Gates");
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

    // Create the output spec and data container.
    final DataTableSpec outSpec = createSpecs(inData[0].getSpec())[0];
    final BufferedDataContainer container = exec.createDataContainer(outSpec);
    final String columnName = modelSettings.getSelectedColumn();
    final int index = outSpec.findColumnIndex(columnName);
    
    DataColumnProperties props = outSpec.getColumnSpec(columnName).getProperties();
    
    if (props.containsProperty(NodeUtilities.KEY_TRANSFORM_MAP)){
        transformSet = TransformSet.loadFromProtoString(props.getProperty(NodeUtilities.KEY_TRANSFORM_MAP));
    } else {
    	throw new CanceledExecutionException("Unable to parse transform map");
    }
        
    List<FCSFrame> dataSet = new ArrayList<>();
    int i = 0;
    for (final DataRow inRow : inData[0]) {
      final DataCell[] outCells = new DataCell[inRow.getNumCells()];
      final FCSFrame inStore = ((FCSFrameFileStoreDataCell) inRow.getCell(index)).getFCSFrameValue();

      // now create the output row
      final FCSFrame df = inStore;
      List<AbstractGate> gates = modelSettings
          .getNodes()
          .values()
          .stream()
          .filter(node -> node instanceof AbstractGate)
          .map(node -> (AbstractGate) node)
          .collect(Collectors.toList());
      gates
        .stream()
        .map(gate -> createSubset(gate, df))
        .forEach(df::addSubset);
      
      final String fsName = NodeUtilities.getFileStoreName(df);
      final FileStore fs = fileStoreFactory.createFileStore(fsName);
      int bytesWritten = NodeUtilities.writeFrameToFilestore(df, fs);
      int summaryFrameSize = (int) (FCSUtilities.DEFAULT_MAX_SUMMARY_FRAME_VALUES/inData[0].size()/df.getDimensionCount());
      BitSet mask = BitSetUtils.getShuffledMask(df.getRowCount(), summaryFrameSize);
      dataSet.add(FCSUtilities.filterFrame(mask, df));
      final FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fs, df, bytesWritten);

      for (int j = 0; j < outCells.length; j++) {
        if (j == index) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      final DataRow outRow = new DefaultRow("Row " + i, outCells);
      container.addRowToTable(outRow);
      exec.setProgress((double)i/inData[0].size());
      exec.checkCanceled();
      exec.setMessage("Reading " + df.getDisplayName());
      i++;
    }
    container.close();
    exec.setMessage("Creating summary frame.");
    BufferedDataTable table = container.getTable();
    String key = FCSUtilities.PROP_KEY_PREVIEW_FRAME;
    FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(dataSet, Integer.MAX_VALUE);
    String value = summaryFrame.saveAsString(); 
    BufferedDataTable finalTable = NodeUtilities.addPropertyToColumn(exec, table, columnName, key, value);
    return new BufferedDataTable[] {finalTable};
  }

  private Subset createSubset(AbstractGate gate, FCSFrame outStore) {
    BitSet mask = gate.evaluate(outStore, transformSet);
    return new Subset(gate.getLabel(), 
        mask, gate.getParentID(), 
        gate.getID(), 
        gate.getType(), 
        gate.getDimensions(), 
        gate.getDescriptors());
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
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {

    modelSettings.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {/*noop*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*noop*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    try {
      modelSettings.save(settings);
    } catch (IOException e) {
      e.printStackTrace();//TODO
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    modelSettings.validate(settings);
  }
}