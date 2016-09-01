package io.landysh.inflor.java.knime.nodes.readFCS;

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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;

/**
 * This is the node model implementation for FCSReader. It is designed to use
 * the Inflor FCSFileReader in the context of a KNIME Source node.
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
	private final SettingsModelString m_FileLocation = new SettingsModelString(CFGKEY_FileLocation,
			DEFAULT_FileLocation);
	private final SettingsModelBoolean m_Compensate = new SettingsModelBoolean(KEY_Compensate, DEFAULT_Compensate);

	protected ReadFCSFrameNodeModel() {
		// Port definition for the node
		super(new PortType[0],
				new PortType[] { PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class) });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ColumnStorePortSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		ColumnStorePortSpec spec = null;
		try {
			final FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue(),
					m_Compensate.getBooleanValue());
			final ColumnStore columnStore = FCSReader.getColumnStore();
			spec = createPortSpec(columnStore);
			FCSReader.close();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Error while checking file. Check that it exists and is valid.");
		}
		return new ColumnStorePortSpec[] { spec };
	}

	private ColumnStorePortSpec createPortSpec(ColumnStore eventsFrame) {
		final ColumnStorePortSpec spec = new ColumnStorePortSpec(eventsFrame.getKeywords(),
				eventsFrame.getColumnNames(), eventsFrame.getRowCount());
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
			FCSReader = new FCSFileReader(m_FileLocation.getStringValue(), m_Compensate.getBooleanValue());
			exec.setProgress(0.1, "header read.");
			exec.checkCanceled();
			FCSReader.readData();
			exec.setProgress(0.9, "data read.");
			final ColumnStore columnStore = FCSReader.getColumnStore();
			final ColumnStorePortSpec spec = createPortSpec(columnStore);
			final ColumnStorePortObject port = ColumnStorePortObject.createPortObject(spec, columnStore, filestore);
			return new PortObject[] { port };
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
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.loadSettingsFrom(settings);
		m_Compensate.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO something?
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_FileLocation.saveSettingsTo(settings);
		m_Compensate.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.validateSettings(settings);
		m_Compensate.validateSettings(settings);
	}
}
