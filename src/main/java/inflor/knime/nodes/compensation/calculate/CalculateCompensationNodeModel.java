package main.java.inflor.knime.nodes.compensation.calculate;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.knime.base.node.io.filereader.DataCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.MatrixUtilities;
import main.java.inflor.knime.ports.compensation.CompMatrixPortObject;
import main.java.inflor.knime.ports.compensation.CompMatrixPortSpec;


/**
 * This is the model implementation of CalculateCompensation.
 * This node attempts to construct a compensation matrix automatically using heuristics to estimate sample roles and Theil-Sen estimation to calculate individual spillover values. 
 *
 * @author Aaron Hart
 */
public class CalculateCompensationNodeModel extends NodeModel {
    
    private static final NodeLogger logger = NodeLogger
            .getLogger(CalculateCompensationNodeModel.class);

    CalculateCompensationNodeSettings mSettings = new CalculateCompensationNodeSettings();


    /**
     * Constructor for the node model.
     */
    protected CalculateCompensationNodeModel() {
      // Port definition for the node
      super(new PortType[0],
          new PortType[] {BufferedDataTable.TYPE, 
                          PortTypeRegistry.getInstance().getPortType(CompMatrixPortObject.class)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
      return createSpecs();
    }

    private PortObjectSpec[] createSpecs() {
      
      DataColumnSpec[] colSpecs = new DataColumnSpec[]{
          new DataColumnSpecCreator("Dimension", StringCell.TYPE).createSpec(),
          new DataColumnSpecCreator("Control Frame", StringCell.TYPE).createSpec()};

      String[] inputDimensions = mSettings.getInDims();
      String[] outputDimensions = mSettings.getFormattedOutDims();
      
      return new PortObjectSpec[] {
          new DataTableSpec(colSpecs),
          new CompMatrixPortSpec(inputDimensions, outputDimensions)};
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws CanceledExecutionException
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
        throws CanceledExecutionException {
      logger.info("Starting Execution");
      mSettings.setContext(exec);
      double[][] spills = mSettings.calculate(); 
      PortObjectSpec[] specs = createSpecs();
      
      BufferedDataContainer container = exec.createDataContainer((DataTableSpec) specs[0]);
      DataCellFactory cellFactory = new DataCellFactory(exec);
      int i=0;
      for (Entry<String, FCSFrame> entry: mSettings.getCompMap().entrySet()){
        RowKey key = new RowKey("Row " + i);
        DataCell[] cells = new DataCell[]{
            cellFactory.createDataCellOfType(StringCell.TYPE, entry.getKey()),
            cellFactory.createDataCellOfType(StringCell.TYPE, entry.getValue().getDisplayName())};
        DataRow row = new DefaultRow(key, cells);
        container.addRowToTable(row);
        i++;
      }
      container.close();
      CompMatrixPortObject port = new CompMatrixPortObject((CompMatrixPortSpec) specs[1], MatrixUtilities.flatten(spills));
      return new PortObject[] {container.getTable(), port};
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
    protected void saveSettingsTo(final NodeSettingsWO settings) {
      mSettings.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
      mSettings.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {/* TODO */}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {/*TODO*/}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {/*TODO*/}
}