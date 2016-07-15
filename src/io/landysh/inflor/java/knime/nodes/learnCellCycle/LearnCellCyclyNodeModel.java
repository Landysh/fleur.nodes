package io.landysh.inflor.java.knime.nodes.learnCellCycle;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of ModelCellCycle. Uses watson pragmatic
 * modeling <citation> to predict the number of cells in each stage of the cell
 * cycle.
 *
 * @author Aaron Hart
 */
public class LearnCellCyclyNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(LearnCellCyclyNodeModel.class);

	/** Settings key for */
	static final String CFGKEY_Column = "Column";

	/**
	 * Constructor for the node model.
	 */
	protected LearnCellCyclyNodeModel() {
		super(1, 2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		// TODO do something here
		logger.info("Node Model Stub... this is not yet implemented !");

		BufferedDataTable stats = null;
		BufferedDataTable data = null;
		return new BufferedDataTable[] { stats, data };
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		// TODO: check if user settings are available, fit to the incoming
		// table structure, and the incoming types are feasible for the node
		// to execute. If the node can execute in its current state return
		// the spec of its output data table(s) (if you can, otherwise an array
		// with null elements), or throw an exception with a useful user message

		DataTableSpec statSpec = createStatSpec();
		DataTableSpec dataSpec = createDataSpec(inSpecs[0]);

		return new DataTableSpec[] { statSpec, dataSpec };
	}

	private DataTableSpec createDataSpec(DataTableSpec inSpec) {
		DataTableSpec newColSpec = createTableSpec(inSpec);
		DataTableSpec outSpec = new DataTableSpec(inSpec, newColSpec);

		return outSpec;
	}

	private DataTableSpec createTableSpec(DataTableSpec inSpec) {
		DataColumnSpec[] colSpecs = new DataColumnSpec[1];
		colSpecs[0] = new DataColumnSpecCreator("CC_Singlets", StringCell.TYPE).createSpec();

		DataTableSpec spec = new DataTableSpec(colSpecs);

		return spec;
	}

	private DataTableSpec createStatSpec() {
		DataColumnSpec[] colSpecs = new DataColumnSpec[2];
		colSpecs[0] = new DataColumnSpecCreator("Property", StringCell.TYPE).createSpec();
		colSpecs[1] = new DataColumnSpecCreator("Value", StringCell.TYPE).createSpec();
		DataTableSpec tableSpec = new DataTableSpec(colSpecs);
		return tableSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		// TODO save user settings to the config object.

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		// TODO load (valid) settings from the config object.
		// It can be safely assumed that the settings are valided by the
		// method below.

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		// TODO check if the settings could be applied to our model
		// e.g. if the count is in a certain range (which is ensured by the
		// SettingsModel).
		// Do not actually set any values of any member variables.

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
