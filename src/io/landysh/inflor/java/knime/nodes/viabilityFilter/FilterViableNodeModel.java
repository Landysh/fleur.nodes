package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.core.viability.ViabilityFilterSettingsModel;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;

/**
 * This is the model implementation of FilterViable.
 * 
 *
 * @author Landysh Co.
 */
public class FilterViableNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(FilterViableNodeModel.class);

	ViabilityFilterSettingsModel m_settings = new ViabilityFilterSettingsModel();

	protected FilterViableNodeModel() {
		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		return new DataTableSpec[] { createSpec(inSpecs[0]) };
	}

	private DataTableSpec createSpec(DataTableSpec dataTableSpec) {
		return dataTableSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		logger.info("Beginning Execution.");
		final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

		// Create the output spec and data container.
		final DataTableSpec outSpec = createSpec(inData[0].getDataTableSpec());
		final BufferedDataContainer container = exec.createDataContainer(outSpec);
		final String columnName = m_settings.getSelectedColumnSettingsModel().getStringValue();
		final int index = outSpec.findColumnIndex(columnName);
		final String viabilityColumn = m_settings.getViabilityColumnSettingsModel().getStringValue();

		int i = 0;
		for (final DataRow inRow : inData[0]) {
			final DataCell[] outCells = new DataCell[inRow.getNumCells()];
			final ColumnStore columnStore = ((ColumnStoreCell) inRow.getCell(index)).getColumnStore();
			final ViabilityModel model = new ViabilityModel(columnStore.getColumnNames());
			final double[] viabilityData = columnStore.getVector(viabilityColumn).getData();
			model.buildModel(viabilityData);
			final BitSet mask = model.scoreModel(viabilityData);

			// now create the output row
			
			final ColumnStore outStore = FCSUtils.filterColumnStore(mask, columnStore);

			final String fsName = i + "ColumnStore.fs";
			final FileStore fileStore = fileStoreFactory.createFileStore(fsName);
			final ColumnStoreCell fileCell = new ColumnStoreCell(fileStore, outStore);

			for (int j = 0; j < outCells.length; j++) {
				if (j == index) {
					outCells[j] = fileCell;
				} else {
					outCells[j] = inRow.getCell(j);
				}
			}
			final DataRow outRow = new DefaultRow("Row " + i, outCells);
			container.addRowToTable(outRow);
			i++;
		}
		container.close();
		return new BufferedDataTable[] { container.getTable() };
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

		m_settings.loadSettingsFrom(settings);
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

		m_settings.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_settings.validateSettings(settings);

	}
}
