package io.landysh.inflor.main.knime.nodes.experimental.extractCompensation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.main.core.compensation.SpilloverCompensator;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.knime.ports.compensation.CompMatrixPortObject;
import io.landysh.inflor.main.knime.ports.compensation.CompMatrixPortSpec;

/**
 * This is the node model implementation for FCSReader. It is designed to use the Inflor
 * FCSFileReader in the context of a KNIME Source node.
 * 
 * @author Aaron Hart
 */
public class ExtractCommpensationNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ExtractCommpensationNodeModel.class);

  // File location
  static final String CFGKEY_FileLocation = "File Location";
  static final String DEFAULT_FileLocation = "";

  private final SettingsModelString m_FileLocation =
      new SettingsModelString(CFGKEY_FileLocation, DEFAULT_FileLocation);


  protected ExtractCommpensationNodeModel() {
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

    CompMatrixPortSpec spec = null;
    try {
      FCSFileReader reader = new FCSFileReader(m_FileLocation.getStringValue());
      spec = createPortSpec(reader.getHeader());
      reader.close();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException(
          "Error while checking file. Check that it exists and is valid.");
    }
    return new CompMatrixPortSpec[] {spec};
  }

  private CompMatrixPortSpec createPortSpec(Map<String, String> map) {
    SpilloverCompensator compr = new SpilloverCompensator(map);
    String[] inDims = compr.getInputDimensions();
    String[] outDims = compr.getOutputDimensions();
    final CompMatrixPortSpec spec = new CompMatrixPortSpec(inDims, outDims);
    return spec;
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
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(m_FileLocation.getStringValue());
      SpilloverCompensator compr = new SpilloverCompensator(reader.getHeader());
      CompMatrixPortSpec spec = createPortSpec(reader.getHeader());
      CompMatrixPortObject port = new CompMatrixPortObject(spec, compr.getSpilloverValues());
      return new PortObject[] {port};
    } catch (final Exception e) {
      e.printStackTrace();
      throw new CanceledExecutionException("Execution Failed. See log for details.");
    }
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
    m_FileLocation.loadSettingsFrom(settings);
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
    m_FileLocation.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    m_FileLocation.validateSettings(settings);
  }
}
