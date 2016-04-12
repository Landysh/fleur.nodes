package io.landysh.inflor.java.knime.nodes.FCSReader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.java.core.EventFrame;
import io.landysh.inflor.java.core.FCSFileReader;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the node model implementation for FCSReader (rows). It is designed to use the Inflor 
 * FCSFileReader in the context of a KNIME Source node which produces a standard KNIME data table.
 * @author Aaron Hart
 */
public class FCSReaderNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(FCSReaderNodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from
	 * the dialog or from a settings file) (package visibility to be usable from
	 * the dialog).
	 */
	static final String CFGKEY_FileLocation = "File Location";
	static final String DEFAULT_FileLocation = "";

	static final String KEY_Compensate = "Compensate on read:";
	static final Boolean DEFAULT_Compensate = false;

	private final SettingsModelString m_FileLocation = new SettingsModelString(CFGKEY_FileLocation,
			DEFAULT_FileLocation);
	
	private final SettingsModelBoolean m_Compensate = new SettingsModelBoolean(KEY_Compensate, DEFAULT_Compensate);

	static FCSFileReader FCS_READER;

	/**
	 * Constructor for the node model.
	 */
	protected FCSReaderNodeModel() {

		// Top port contains header information, bottom, array data
		super(0, 2);
	}

	/**
	 * {@inheritDoc}
	 * @throws CanceledExecutionException 
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws CanceledExecutionException
			{

		logger.info("Starting Execution");
		// get table specs
		FCSFileReader FCSReader;
		BufferedDataContainer header = null;
		BufferedDataContainer data = null;
		try {
			FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
			EventFrame frame = FCSReader.getEventFrame();
			Hashtable <String, String> keywords = FCSReader.getHeader();
			DataTableSpec[] tableSpecs = createPortSpecs(frame);
			// Read header section
			header = exec.createDataContainer(tableSpecs[0]);
			Enumeration<String> enumKey = keywords.keys();
			int i = 0;
			while (enumKey.hasMoreElements()) {
				String key = enumKey.nextElement();
				String val = keywords.get(key);
				RowKey rowKey = new RowKey("Row " + i);
				// the cells of the current row, the types of the cells must match
				// the column spec (see above)
				DataCell[] keywordCells = new DataCell[2];
				keywordCells[0] = new StringCell(key);
				keywordCells[1] = new StringCell(val);
				DataRow keywordRow = new DefaultRow(rowKey, keywordCells);
				header.addRowToTable(keywordRow);
				i++;
				if (key.equals("0") && val.equals("0"))
					keywords.remove(key);
			}
			header.close();

			// a quick breath before we move on.
			exec.checkCanceled();
			exec.setProgress(0.01, "Header read.");

			// Read data section
			data = exec.createDataContainer(tableSpecs[1]);
			FCSReader.initRowReader();
			for (Integer j = 0; j<frame.eventCount; j++) {
				RowKey rowKey = new RowKey(j.toString());
				DataCell[] dataCells = null;
				if(m_Compensate.getBooleanValue()==true){
					dataCells = new DataCell[frame.parameterCount + frame.compParameters.length];
				} else {
					dataCells = new DataCell[frame.parameterCount];
				}

				double[] FCSRow = FCSReader.readRow();
				//for each uncomped parameter
				int k=0;
				while ( k<frame.parameterCount) {
					// add uncomped data
					dataCells[k] = new DoubleCell(FCSRow[k]);
					k++;
				}
				//for each comped parameter
				if(m_Compensate.getBooleanValue()==true){	
					double[] FCSCompRow = frame.doCompRow(FCSRow);
					for (int l=0;l<FCSCompRow.length;l++){
						dataCells[frame.parameterCount+l] = new DoubleCell(FCSCompRow[l]);
					}
				}
				//dataCells[frame.parameterCount+frame.compParameters.length] = new DoubleCell(new Double(j));
				DataRow dataRow = new DefaultRow(rowKey, dataCells);
				data.addRowToTable(dataRow);
				if (j % 100 == 0) {
					exec.checkCanceled();
					exec.setProgress(j / (double)frame.eventCount, j + " rows read.");
				}
			}
			// once we are done, we close the container and return its table
			data.close();
		} catch (Exception e) {
			exec.setMessage("Execution Failed while reading data file.");
			e.printStackTrace();
			throw new CanceledExecutionException("Execution Failed while reading data file.");
		}
		
		return new BufferedDataTable[] { header.getTable(), data.getTable() };
	}

	private DataTableSpec[] createPortSpecs(EventFrame frame) {
		DataTableSpec[] specs = new DataTableSpec[2];
		specs[0] = createKeywordSpec();
		specs[1] = createDataSpec(frame);
		return specs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		FCS_READER = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		DataTableSpec[] specs = null;
		try {
			FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
			EventFrame eventsFrame = FCSReader.getEventFrame();
			specs = createPortSpecs(eventsFrame);
			FCSReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Error while checking file. Check that it exists and is valid.");
		}
		return specs;
	}

	private DataTableSpec createDataSpec(EventFrame frame) {
		int parCount = frame.parameterCount;
		int compParCount = 0;
		String[] compPars = null;
		// get comp info if available
		if (m_Compensate.getBooleanValue()==true){
			compParCount = frame.compParameters.length;
			compPars = frame.compParameters;
		}
		DataColumnSpec[] colSpecs = new DataColumnSpec[parCount+compParCount];
		String[] columnNames = frame.getDisplayColumnNames();
		int i = 0;
		while (i<columnNames.length) {
			colSpecs[i] = new DataColumnSpecCreator(columnNames[i], DoubleCell.TYPE).createSpec();
			i++;
		}
		if (m_Compensate.getBooleanValue()==true){
			for (int j=0; j<compPars.length; j++) {
				colSpecs[parCount + j] = new DataColumnSpecCreator("Comp::" + compPars[j], DoubleCell.TYPE).createSpec();
			}
		}

		DataTableSpec dataSpec = new DataTableSpec(colSpecs);
		return dataSpec;
	}

	private DataTableSpec createKeywordSpec() {
		DataColumnSpec[] colSpecs = new DataColumnSpec[2];
		colSpecs[0] = new DataColumnSpecCreator("keyword", StringCell.TYPE).createSpec();
		colSpecs[1] = new DataColumnSpecCreator("value", StringCell.TYPE).createSpec();

		DataTableSpec headerSpec = new DataTableSpec(colSpecs);
		return headerSpec;
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.loadSettingsFrom(settings);
		m_Compensate.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_FileLocation.validateSettings(settings);
		m_Compensate.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// TODO load internal data.
		// Everything handed to output ports is loaded automatically (data
		// returned by the execute method, models loaded in loadModelContent,
		// and user settings set through loadSettingsFrom - is all taken care
		// of). Load here only the other internals that need to be restored
		// (e.g. data used by the views).

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// TODO save internal models.
		// Everything written to output ports is saved automatically (data
		// returned by the execute method, models saved in the saveModelContent,
		// and user settings saved through saveSettingsTo - is all taken care
		// of). Save here only the other internals that need to be preserved
		// (e.g. data used by the views).

	}

}
