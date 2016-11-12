package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import org.knime.core.data.DataCell;
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

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.gates.GateUtilities;
import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.knime.dataTypes.FCSFrameCell.FCSFrameCell;

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

  /**
   * Constructor for the node model.
   */
  protected CreateGatesNodeModel() {
    super(1, 1);

    modelSettings = new CreateGatesNodeSettings();

  }

  private FCSFrame applyGates(FCSFrame inStore) {
    List<AbstractGate> gates = modelSettings.findGates(inStore.getID());
    BitSet returnMe = GateUtilities.applyGatingPath(inStore, gates);
    FCSFrame outStore = FCSUtilities.filterColumnStore(returnMe, inStore);
    return outStore;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {

    final DataTableSpec[] outSpecs = createSpecs(inSpecs[0]);

    return outSpecs;
  }

  private DataTableSpec[] createSpecs(DataTableSpec inSpec) {
    final DataTableSpecCreator creator = new DataTableSpecCreator(inSpec);
    final DataTableSpec outSpec = creator.createSpec();
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

    int i = 0;
    for (final DataRow inRow : inData[0]) {
      final DataCell[] outCells = new DataCell[inRow.getNumCells()];
      final FCSFrame inStore = ((FCSFrameCell) inRow.getCell(index)).getFCSFrame();

      // now create the output row
      final FCSFrame outStore = applyGates(inStore);
      final String fsName = i + "ColumnStore.fs";
      final FileStore fileStore = fileStoreFactory.createFileStore(fsName);
      final FCSFrameCell fileCell = new FCSFrameCell(fileStore, outStore);

      for (int j = 0; j < outCells.length; j++) {
        if (j == index) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      final DataRow outRow = new DefaultRow("Row " + i, outCells);
      container.addRowToTable(outRow);
      i++;
    }
    container.close();
    return new BufferedDataTable[] {container.getTable()};
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

    modelSettings.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    // TODO Code executed on reset.
    // Models build during execute are cleared here.
    // Also data handled in load/saveInternals will be erased here.
  }

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

    try {
      modelSettings.save(settings);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
// EOF
