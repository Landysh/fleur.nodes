package fleur.knime.nodes.compensation.extract.fcs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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

import fleur.core.compensation.SpilloverCompensator;
import fleur.core.fcs.FCSFileReader;
import fleur.knime.ports.compensation.CompMatrixPortObject;
import fleur.knime.ports.compensation.CompMatrixPortSpec;

/**
 * This is the node model implementation for FCSReader. It is designed to use the Inflor
 * FCSFileReader in the context of a KNIME Source node.
 * 
 * @author Aaron Hart
 */
public class ExtractCommpensationNodeModel extends NodeModel {

  private static final String ERROR_EXECUTION_FAILED = "ERROR: Execution failed. See log for details";
  // the logger instance and errors.
  private static final NodeLogger logger = NodeLogger.getLogger(ExtractCommpensationNodeModel.class);
  private static final String ERROR_NO_VALID_FILE = "Error: no valid file.";

  // File location
  public static final String KEY_FILE_LOCATION = "File Location";
  public static final String DEFAULT_FILE_LOCATION = "";
  private final SettingsModelString mFileLocation =
      new SettingsModelString(KEY_FILE_LOCATION, DEFAULT_FILE_LOCATION);

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

    String path = mFileLocation.getStringValue();

    CompMatrixPortSpec spec;
    try (RandomAccessFile raf = new RandomAccessFile(path, "r")){
      FCSFileReader reader = new FCSFileReader(path, raf);
      Map<String, String> header = reader.getHeader();
      spec = createPortSpec(header);
      reader.close();
      return new CompMatrixPortSpec[] {spec};
    } catch (final Exception e) {
      logger.error(ERROR_NO_VALID_FILE, e);
      throw new InvalidSettingsException(ERROR_NO_VALID_FILE);
    }
  }

  private CompMatrixPortSpec createPortSpec(Map<String, String> map) {
    SpilloverCompensator compr = new SpilloverCompensator(map);
    String[] inDims = compr.getInputDimensions();
    String[] outDims = compr.getOutputDimensions();
    return new CompMatrixPortSpec(inDims, outDims);
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
    String path = mFileLocation.getStringValue();

    try  (RandomAccessFile raf = new RandomAccessFile(path, "r")){
      reader = new FCSFileReader(path, raf);
      SpilloverCompensator compr = new SpilloverCompensator(reader.getHeader());
      CompMatrixPortSpec spec = createPortSpec(reader.getHeader());
      CompMatrixPortObject port = new CompMatrixPortObject(spec, compr.getSpilloverValues());
      return new PortObject[] {port};
    } catch (Exception e) {
      logger.error(ERROR_EXECUTION_FAILED, e);
      throw new CanceledExecutionException(ERROR_EXECUTION_FAILED);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {  /*TODO*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mFileLocation.loadSettingsFrom(settings);
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
    mFileLocation.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    mFileLocation.validateSettings(settings);
  }
}
