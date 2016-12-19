package io.landysh.inflor.main.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
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

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.knime.portTypes.fcsFrame.FCSFramePortObject;
import io.landysh.inflor.main.knime.portTypes.fcsFrame.FCSFramePortSpec;

/**
 * This is the node model implementation for FCSReader. It is designed to use the Inflor
 * FCSFileReader in the context of a KNIME Source node.
 * 
 * @author Aaron Hart
 */
public class ReadFCSFrameNodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSFrameNodeModel.class);

  // File location
  static final String CFGKEY_FileLocation = "File Location";
  static final String DEFAULT_FileLocation = "";
  // Compensate while reading
  static final String KEY_Compensate = "Compensate on read:";

  static final Boolean DEFAULT_Compensate = false;
  private final SettingsModelString m_FileLocation =
      new SettingsModelString(CFGKEY_FileLocation, DEFAULT_FileLocation);


  protected ReadFCSFrameNodeModel() {
    // Port definition for the node
    super(new PortType[0],
        new PortType[] {PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)});
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected FCSFramePortSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {

    FCSFramePortSpec spec = null;
    try {
      final FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
      final FCSFrame columnStore = FCSReader.getColumnStore();
      spec = createPortSpec(columnStore);
      FCSReader.close();
    } catch (final Exception e) {
      e.printStackTrace();
      throw new InvalidSettingsException(
          "Error while checking file. Check that it exists and is valid.");
    }
    return new FCSFramePortSpec[] {spec};
  }

  private FCSFramePortSpec createPortSpec(FCSFrame eventsFrame) {
    final FCSFramePortSpec spec = new FCSFramePortSpec(eventsFrame.getKeywords(),
        eventsFrame.getColumnNames().toArray(new String[eventsFrame.getColumnCount()]), eventsFrame.getRowCount());
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
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    logger.info("Starting Execution");
    FCSFileReader FCSReader;
    try {
      final FileStore filestore = fileStoreFactory.createFileStore("column.store");
      FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
      exec.setProgress(0.1, "header read.");
      exec.checkCanceled();
      FCSReader.readData();
      exec.setProgress(0.9, "data read.");
      final FCSFrame columnStore = FCSReader.getColumnStore();
      final FCSFramePortSpec spec = createPortSpec(columnStore);
      final FCSFramePortObject port =
          FCSFramePortObject.createPortObject(spec, columnStore, filestore);
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
