package io.landysh.inflor.java.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.gatingML.compensation.SpilloverCompensator;
import io.landysh.inflor.java.core.utils.FCSUtils;

/**
 * This is the node model implementation for FCSReader (rows). It is designed to
 * use the Inflor FCSFileReader in the context of a KNIME Source node which
 * produces a standard KNIME data table.
 * 
 * @author Aaron Hart
 */
public class ReadFCSTableNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSTableNodeModel.class);

	// File Location
	static final String CFGKEY_FileLocation = "File Location";
	static final String DEFAULT_FileLocation = null;
	// Compensate while reading
	static final String KEY_Compensate = "Compensate on read:";

	static final Boolean DEFAULT_Compensate = false;
	private final SettingsModelString m_FileLocation = new SettingsModelString(CFGKEY_FileLocation,
			DEFAULT_FileLocation);
	private final SettingsModelBoolean m_Compensate = new SettingsModelBoolean(KEY_Compensate, DEFAULT_Compensate);

	/**
	 * Constructor for the node model.
	 */
	protected ReadFCSTableNodeModel() {

		// Top port contains header information, bottom, array data
		super(0, 2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (m_FileLocation.getStringValue() == null) {
			throw new InvalidSettingsException("There is no file to read. Please select a valid FCS file.");
		}
		DataTableSpec[] specs = null;
		try {
			final FCSFileReader FCSReader = new FCSFileReader(m_FileLocation.getStringValue(),
					m_Compensate.getBooleanValue());
			final ColumnStore eventsFrame = FCSReader.getColumnStore();
			specs = createPortSpecs(eventsFrame);
			FCSReader.close();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Error while checking file. Check that it exists and is valid.");
		}
		return specs;
	}

	private DataTableSpec createDataSpec(ColumnStore columnStore) throws InvalidSettingsException {
		final String[] columnNames = columnStore.getColumnNames();
		final DataColumnSpec[] colSpecs = new DataColumnSpec[columnNames.length];
		for (final String columnName : columnNames) {
			final int specIndex = FCSUtils.findParameterNumnberByName(columnStore.getKeywords(), columnName) - 1;
			colSpecs[specIndex] = new DataColumnSpecCreator(columnName, DoubleCell.TYPE).createSpec();
		}
		final DataTableSpec dataSpec = new DataTableSpec(colSpecs);
		return dataSpec;
	}

	private DataTableSpec createKeywordSpec() {
		final DataColumnSpec[] colSpecs = new DataColumnSpec[2];
		colSpecs[0] = new DataColumnSpecCreator("keyword", StringCell.TYPE).createSpec();
		colSpecs[1] = new DataColumnSpecCreator("value", StringCell.TYPE).createSpec();

		final DataTableSpec headerSpec = new DataTableSpec(colSpecs);
		return headerSpec;
	}

	private DataTableSpec[] createPortSpecs(ColumnStore frame) throws InvalidSettingsException {
		final DataTableSpec[] specs = new DataTableSpec[2];
		specs[0] = createKeywordSpec();
		specs[1] = createDataSpec(frame);
		return specs;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws CanceledExecutionException
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws CanceledExecutionException {

		logger.info("Starting Execution");
		// get table specs
		FCSFileReader FCSReader;
		BufferedDataContainer headerTable = null;
		BufferedDataContainer dataTable = null;

		try {
			FCSReader = new FCSFileReader(m_FileLocation.getStringValue(), m_Compensate.getBooleanValue());
			final HashMap<String, String> keywords = FCSReader.getHeader();

			final ColumnStore columnStore = FCSReader.getColumnStore();
			final DataTableSpec[] tableSpecs = createPortSpecs(columnStore);

			// Read header section
			headerTable = exec.createDataContainer(tableSpecs[0]);
			readHeader(headerTable, keywords);

			// check in with the boss before we move on.
			exec.checkCanceled();
			exec.setProgress(0.01, "Header read.");

			// Initialize the compensator.
			final SpilloverCompensator compensator = new SpilloverCompensator(keywords);

			// Read data section
			dataTable = exec.createDataContainer(tableSpecs[1]);
			FCSReader.initRowReader();
			for (Integer j = 0; j < columnStore.getRowCount(); j++) {
				final RowKey rowKey = new RowKey(j.toString());
				DataCell[] dataCells = null;
				if (m_Compensate.getBooleanValue() == true) {
					dataCells = new DataCell[columnStore.getColumnCount() + compensator.getCompParameterNames().length];
				} else {
					dataCells = new DataCell[columnStore.getColumnCount()];
				}

				final double[] FCSRow = FCSReader.readRow();
				// for each uncomped parameter
				int k = 0;
				while (k < columnStore.getColumnCount()) {
					// add uncomped data
					dataCells[k] = new DoubleCell(FCSRow[k]);
					k++;
				}
				// for each comped parameter
				if (m_Compensate.getBooleanValue() == true) {
					final double[] FCSCompRow = compensator.compensateRow(FCSRow);
					for (int l = 0; l < FCSCompRow.length; l++) {
						dataCells[columnStore.getColumnCount() + l] = new DoubleCell(FCSCompRow[l]);
					}
				}
				final DataRow dataRow = new DefaultRow(rowKey, dataCells);
				dataTable.addRowToTable(dataRow);
				if (j % 100 == 0) {
					exec.checkCanceled();
					exec.setProgress(j / (double) columnStore.getRowCount(), j + " rows read.");
				}
			}
			// once we are done, we close the container and return its table
			dataTable.close();
		} catch (final Exception e) {
			exec.setMessage("Execution Failed while reading data file.");
			e.printStackTrace();
			throw new CanceledExecutionException("Execution Failed while reading data file.");
		}

		return new BufferedDataTable[] { headerTable.getTable(), dataTable.getTable() };
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

	private void readHeader(BufferedDataContainer header, HashMap<String, String> keywords) {
		final Enumeration<String> enumKey = keywords.keys();
		int i = 0;
		while (enumKey.hasMoreElements()) {
			final String key = enumKey.nextElement();
			final String val = keywords.get(key);
			final RowKey rowKey = new RowKey("Row " + i);
			// the cells of the current row, the types of the cells must match
			// the column spec (see above)
			final DataCell[] keywordCells = new DataCell[2];
			keywordCells[0] = new StringCell(key);
			keywordCells[1] = new StringCell(val);
			final DataRow keywordRow = new DefaultRow(rowKey, keywordCells);
			header.addRowToTable(keywordRow);
			i++;
			if (key.equals("0") && val.equals("0"))
				keywords.remove(key);
		}
		header.close();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
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
