package main.java.inflor.knime.nodes.compensation.calculate;

import java.io.File;
import java.io.IOException;

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
          new PortType[] {PortTypeRegistry.getInstance().getPortType(CompMatrixPortObject.class)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CompMatrixPortSpec[] configure(final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException {
      String[] inputDimensions = mSettings.getInDims();
      String[] outputDimensions = mSettings.getFormattedOutDims();
      return new CompMatrixPortSpec[] {new CompMatrixPortSpec(inputDimensions, outputDimensions)};
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
  
      
      
      double[][] spills = mSettings.compCalculator.calculate();
      
      CompMatrixPortSpec spec = new CompMatrixPortSpec(mSettings.getInDims(), mSettings.getFormattedOutDims());
      CompMatrixPortObject port = new CompMatrixPortObject(spec, MatrixUtilities.flatten(spills));
      return new PortObject[] {port};
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