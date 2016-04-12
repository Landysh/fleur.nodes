package io.landysh.inflor.java.knime.nodes.FCSFrameReader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

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

import io.landysh.inflor.java.core.EventFrame;
import io.landysh.inflor.java.core.FCSFileReader;
import io.landysh.inflor.java.knime.portTypes.fcs.FCSFrameSpec;
import io.landysh.inflor.java.knime.portTypes.fcs.FCSFramePortObject;

/**
 * This is the node model implementation for FCSReader (rows). It is designed to use the Inflor 
 * FCSFileReader in the context of a KNIME Source node which produces a standard KNIME data table.
 * @author Aaron Hart
 */
public class FCSFrameReaderNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(FCSFrameReaderNodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from
	 * the dialog or from a settings file) (package visibility to be usable from
	 * the dialog).
	 */
	static final String CFGKEY_FileLocation = "File Location";

	/** initial default count value. */
	static final String DEFAULT_FileLocation = "";

	// example value: the models count variable filled from the dialog
	// and used in the models execution method. The default components of the
	// dialog work with "SettingsModels".
	private final SettingsModelString m_FileLocation = new SettingsModelString(CFGKEY_FileLocation,
			DEFAULT_FileLocation);

	static FCSFileReader FCS_READER;

	/**
	 * Constructor for the node model.
	 */
	protected FCSFrameReaderNodeModel() {

		// Top port contains header information, bottom, array data
        super(new PortType[0], new PortType[]{PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)});
	}

	/**
	 * {@inheritDoc}
	 * @throws CanceledExecutionException 
	 */
	@Override
	 protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws CanceledExecutionException
			{

		logger.info("Starting Execution");
		// get table specs
		FCSFileReader FCSReader;
		try {
			FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
			EventFrame frame = FCSReader.getEventFrame();
			exec.setProgress(0.1, "header read.");
			exec.checkCanceled();
			FCSReader.readColumnEventData();
			exec.setProgress(0.6, "data read.");
			FCSFrameSpec spec = createPortSpec(frame);
			Hashtable<String, double[]> columns = FCSReader.getColumnStore();
			FCSFramePortObject port = new FCSFramePortObject(spec, columns);
			return new PortObject[] {port};
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CanceledExecutionException("Execution Failed.");
		}	
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
    protected FCSFrameSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		FCSFrameSpec spec = null;
		try {
			FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
			EventFrame eventsFrame = FCSReader.getEventFrame();
			spec = createPortSpec(eventsFrame);
			FCSReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Error while checking file. Check that it exists and is valid.");
		}
		return new FCSFrameSpec[]{spec};
	}


	private FCSFrameSpec createPortSpec(EventFrame eventsFrame) {
		FCSFrameSpec spec = new FCSFrameSpec(eventsFrame.getHeader(), eventsFrame.getCannonColumnNames());
		return spec;
	}



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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.loadSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.validateSettings(settings);

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
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {


	}

}
