package io.landysh.inflor.java.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.google.common.base.Joiner;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;

/**
 * This is the model implementation of ReadFCSSet.
 * 
 *
 * @author Landysh Co.
 */
public class ReadFCSSetNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(ReadFCSSetNodeModel.class);

	// Folder containing FCS Files.
	static final String CFGKEY_PATH = "Path";
	static final String DEFAULT_PATH = null;
	private final SettingsModelString m_path = new SettingsModelString(CFGKEY_PATH, DEFAULT_PATH);

	// Should we compensate?
	static final String CFGKEY_COMPENSATE = "Compensate";
	static final boolean DEFAULT_COMPENSATE = false;
	private final SettingsModelBoolean m_compensate = new SettingsModelBoolean(CFGKEY_COMPENSATE, DEFAULT_COMPENSATE);

	/**
	 * Constructor for the node model.
	 */
	protected ReadFCSSetNodeModel() {
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		logger.info("Beginning Execution.");

		// Create the output spec and data container.
		DataTableSpec outSpec = createSpec();
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		String[] filePaths = getFilePaths(m_path.getStringValue());
		int rowCount = filePaths.length;

		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

		// Read all the files.
		for (int i = 0; i < rowCount; i++) {
			RowKey key = new RowKey("Row " + i);
			String pathToFile = filePaths[i];
			FCSFileReader FCSReader = new FCSFileReader(pathToFile, m_compensate.getBooleanValue());
			FCSReader.readData();
			ColumnStore columnStore = FCSReader.getColumnStore();
			String fsName = i + "ColumnStore.fs";
			FileStore fileStore = fileStoreFactory.createFileStore(fsName);
			ColumnStoreCell fileCell = new ColumnStoreCell(fileStore, columnStore);
			DataCell[] cells = new DataCell[] { fileCell };

			DataRow row = new DefaultRow(key, cells);
			container.addRowToTable(row);

			// check if the execution monitor was canceled
			exec.checkCanceled();
			exec.setProgress(i / (double) rowCount, "Reading file " + (i + 1));
		}
		// once we are done, we close the container and return its table
		container.close();
		BufferedDataTable out = container.getTable();
		return new BufferedDataTable[] { out };
	}

	private DataTableSpec createSpec() throws Exception {
		DataColumnSpec[] colSpecs = new DataColumnSpec[] { createFCSColumnSpec() };
		DataTableSpec tableSpec = new DataTableSpec(colSpecs);
		return tableSpec;
	}

	private DataColumnSpec createFCSColumnSpec() throws Exception {
		DataColumnSpecCreator creator = new DataColumnSpecCreator("FCS Frame", ColumnStoreCell.TYPE);
		// Create properties
		Hashtable<String, String> content = createColumnPropertiesContent();
		DataColumnProperties properties = new DataColumnProperties(content);
		creator.setProperties(properties);
		// Create spec
		DataColumnSpec dcs = creator.createSpec();
		return dcs;
	}

	private Hashtable<String, String> createColumnPropertiesContent() throws Exception {
		/**
		 * Creates column properties for an FCS Set by looking all of the
		 * headers and setting shared keyword values.
		 */
		Hashtable<String, ArrayList<String>> contentArray = new Hashtable<String, ArrayList<String>>();
		String[] filePaths = getFilePaths(m_path.getStringValue());
		// For each file
		for (String path : filePaths) {
			FCSFileReader FCSReader = new FCSFileReader(path, false);
			Hashtable<String, String> header = FCSReader.getHeader();
			Enumeration<String> keys = header.keys();
			// for each keyword
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = header.get(key);
				// If the keyword has been seen already
				if (contentArray.containsKey(key)) {
					// See if the value is new, if it isn't
					if (contentArray.get(key).contains(value)) {
						// Do nothing
					} else {
						// otherwise add it.
						contentArray.get(key).add(value);
					}
				} else {
					// if the keyword is new, add it and its value.
					ArrayList<String> values = new ArrayList<String>();
					values.add(value);
					contentArray.put(key, values);
				}
			}
		}
		// Now aggregate into a reasonable Hashtable<String String>
		Hashtable<String, String> content = new Hashtable<String, String>();
		Enumeration<String> contentKeys = contentArray.keys();

		while (contentKeys.hasMoreElements()) {
			String key = contentKeys.nextElement();
			ArrayList<String> values = contentArray.get(key);
			if (values.size() == 1) {
				content.put(key, values.get(0));
			} else {
				String newVal = Joiner.on(',').join(values);
				content.put(key, newVal);
			}
		}
		return content;
	}

	private String[] getFilePaths(String dirPath) {
		/**
		 * Returns a list of valid FCS Files from the chose directory.
		 */
		File folder = new File(dirPath);
		File[] files = folder.listFiles();
		ArrayList<String> validFiles = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			String filePath = files[i].getAbsolutePath();
			if (FCSFileReader.isValidFCS(filePath) == true) {
				validFiles.add(filePath);
			} else if (files[i].isDirectory()) {
				System.out.println("Directory " + files[i].getName());
			}
		}
		return (String[]) validFiles.toArray(new String[validFiles.size()]);
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec spec;
		try {
			spec = createSpec();
		} catch (Exception e) {
			InvalidSettingsException ise = new InvalidSettingsException(
					"Unable to read headers of 1 or more FCS Files.");
			ise.printStackTrace();
			throw ise;
		}
		return new DataTableSpec[] { spec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_path.saveSettingsTo(settings);
		// m_selectedKeywords.saveSettingsTo(settings);
		m_compensate.saveSettingsTo(settings);
		// m_selectedFiles.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_path.loadSettingsFrom(settings);
		// m_selectedKeywords.loadSettingsFrom(settings);
		m_compensate.loadSettingsFrom(settings);
		// m_selectedFiles.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_path.validateSettings(settings);
		// m_selectedKeywords.validateSettings(settings);
		m_compensate.validateSettings(settings);
		// m_selectedFiles.validateSettings(settings);
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