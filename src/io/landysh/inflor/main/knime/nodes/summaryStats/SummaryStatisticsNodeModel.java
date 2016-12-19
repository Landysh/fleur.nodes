package io.landysh.inflor.main.knime.nodes.summaryStats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
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

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of SummaryStatistics. Extract basic summary statistics from a
 * set of FCS Files.
 *
 * @author Aaron Hart
 */
public class SummaryStatisticsNodeModel extends NodeModel {

  SummaryStatsSettings modelSettings = new SummaryStatsSettings();
  private static final NodeLogger logger = NodeLogger.getLogger(SummaryStatisticsNodeModel.class);

  /**
   * Constructor for the node model.
   */
  protected SummaryStatisticsNodeModel() {

    super(1, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    final DataTableSpec outSpec = createSpec(inSpecs[0]);
    return new DataTableSpec[] {outSpec};
  }

  private DataTableSpec createSpec(DataTableSpec inSpec) {
    DataTableSpecCreator creator = new DataTableSpecCreator(inSpec);
    List<DataColumnSpec> cspecs = new ArrayList<DataColumnSpec>();
    for (StatSpec spec: modelSettings.getStatSpecs()){
      DataColumnSpec cspec = new DataColumnSpecCreator(spec.toString(), DoubleCell.TYPE).createSpec();
      cspecs.add(cspec);
    }
    DataColumnSpec[] columns = cspecs.toArray(new DataColumnSpec[cspecs.size()]);
    creator.addColumns(columns);
    return creator.createSpec();
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
    DataTableSpec outSpec = createSpec(inData[0].getSpec());
    BufferedDataContainer container = exec.createDataContainer(outSpec);
    String columnName = modelSettings.getSelectedColumn();
    List<StatSpec> statDefinitions = modelSettings.getStatSpecs();
    int index = outSpec.findColumnIndex(columnName);
    

    int i = 0;
    for (final DataRow inRow : inData[0]) {
      DataCell[] outCells = new DataCell[inRow.getNumCells() + statDefinitions.size()];
      FCSFrame inStore = ((FCSFrameFileStoreDataCell) inRow.getCell(index)).getFCSFrameValue();

      // now create the output row
      FCSFrame outStore = inStore.deepCopy();
      
      String fsName = i + "ColumnStore.fs";
      FileStore fileStore = fileStoreFactory.createFileStore(fsName);
      FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fileStore, outStore);
      inRow.getNumCells();
      for (int j = 0; j < inRow.getNumCells(); j++) {
        if (j == index) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      
      //calculate the statistics.
      for (StatSpec stat : statDefinitions){
        Double value = stat.evaluate(outStore);
        int statIndex = outSpec.findColumnIndex(stat.toString());
        outCells[statIndex] = new DoubleCell(value);
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
    modelSettings.save(settings);
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
