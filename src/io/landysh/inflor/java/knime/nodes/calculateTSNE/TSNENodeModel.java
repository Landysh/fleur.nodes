package io.landysh.inflor.java.knime.nodes.calculateTSNE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.MatrixOps;
import com.jujutsu.tsne.TSne;


/**
 * This is the model implementation of the TSNE node for the KNIME Analytics
 * Platform. Calculates a tSNE using library developed by Leif Jonsson:
 * https://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNENodeModel extends NodeModel {

	// Column filter
	static final String CFGKEY_Columns = "Columns";

	private SettingsModelColumnFilter2 modelColumns = new SettingsModelColumnFilter2(CFGKEY_Columns);

	// Iterations
	static final String CFGKEY_Iterations = "Iterations";
	static final Integer MIN_Iterations = 10;
	static final Integer MAX_Iterations = 500;
	static final Integer DEFAULT_Iterations = 250;

	private final SettingsModelIntegerBounded modelIterations = new SettingsModelIntegerBounded(CFGKEY_Iterations,
			DEFAULT_Iterations, MIN_Iterations, MAX_Iterations);

	// PCA Dims
	static final String CFGKEY_InitDims = "PCA Dimensions";
	static final Integer MIN_InitDims = 0;
	static final Integer MAX_InitDims = 100;
	static final Integer DEFAULT_InitDims = 10;

	private final SettingsModelIntegerBounded modelInitDims = new SettingsModelIntegerBounded(CFGKEY_InitDims,
			DEFAULT_InitDims, MIN_InitDims, MAX_InitDims);

	// Perplexity
	static final String CFGKEY_Perplexity = "Maximum iterations";
	static final Double MIN_Perplexity = 1.;
	static final Double MAX_Perplexity = 100.;
	static final Double DEFAULT_Perplexity = 20.;

	private final SettingsModelDoubleBounded modelPerplexity = new SettingsModelDoubleBounded(CFGKEY_Perplexity,
			DEFAULT_Perplexity, MIN_Perplexity, MAX_Perplexity);

	/**
	 * Constructor for the node model.
	 */
	protected TSNENodeModel() {

		super(1, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		double[][] data = readData(inData[0], exec);
		// data = MatrixOps.log(data, true);
		data = MatrixOps.centerAndScale(data);

		TSne tsne = new FastTSne();
		
		double[][] Y = tsne.tsne(data, 2, -1, modelPerplexity.getDoubleValue(), modelIterations.getIntValue(), false);

		DataTableSpec newColSpec = createTableSpec(inData[0].getSpec());
		DataTableSpec spec = new DataTableSpec(inData[0].getSpec(), newColSpec);

		BufferedDataContainer container = exec.createDataContainer(spec);
		CloseableRowIterator rowIterator = inData[0].iterator();

		long rowCount = 0;
		while (rowIterator.hasNext()) {
			RowKey rowKey = new RowKey("Row " + rowCount);
			DataRow inCols = rowIterator.next();
			double[] tsneCols = Y[(int) rowCount];
			DoubleCell tsne1 = new DoubleCell(tsneCols[0]);
			DoubleCell tsne2 = new DoubleCell(tsneCols[1]);
			DataCell[] tsneCells = new DataCell[] { tsne1, tsne2 };
			DataRow tsneRow = new DefaultRow(rowKey, tsneCells);
			ArrayList<DataCell> cells = new ArrayList<DataCell>(inCols.getNumCells() + tsneRow.getNumCells());
			for (DataCell cell : inCols) {
				cells.add(cell);
			}
			for (DataCell cell : tsneRow) {
				cells.add(cell);
			}
			DataRow outRow = new DefaultRow(rowKey, cells);
			container.addRowToTable(outRow);
			rowCount++;
		}
		container.close();
		return new BufferedDataTable[] { container.getTable() };
	}

	private double[][] readData(BufferedDataTable inData, ExecutionContext exec) throws Exception {
		long rowCount = inData.size();
		if (rowCount > Integer.MAX_VALUE) {
			throw new Exception("Too many rows, must use fewer that: " + Integer.MAX_VALUE
					+ " larger tables are unsupported and probably unwise for this algorithm");
		}
		String[] columns = modelColumns.applyTo(inData.getSpec()).getIncludes();
		double[][] dataTable = new double[(int) rowCount][columns.length];
		int i = 0;
		for (DataRow inRow : inData) {
			double[] outRow = new double[columns.length];
			for (int j = 0; j < columns.length; j++) {
				int specIndex = inData.getSpec().findColumnIndex(columns[j]);
				DataCell cell = inRow.getCell(specIndex);
				outRow[j] = ((DoubleValue) cell).getDoubleValue();
			}
			dataTable[i] = outRow;
			i++;
		}
		return dataTable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpec) throws InvalidSettingsException {
		DataTableSpec newColSpec = createTableSpec(inSpec[0]);
		DataTableSpec outSpec = new DataTableSpec(inSpec[0], newColSpec);
		return new DataTableSpec[] { outSpec };
	}

	private DataTableSpec createTableSpec(DataTableSpec inSpec) {

		DataColumnSpec[] colSpecs = new DataColumnSpec[2];
		colSpecs[0] = new DataColumnSpecCreator("TSNE1", DoubleCell.TYPE).createSpec();
		colSpecs[1] = new DataColumnSpecCreator("TSNE2", DoubleCell.TYPE).createSpec();

		DataTableSpec spec = new DataTableSpec(colSpecs);

		return spec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		modelColumns.saveSettingsTo(settings);
		modelIterations.saveSettingsTo(settings);
		modelPerplexity.saveSettingsTo(settings);
		modelInitDims.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		modelColumns.loadSettingsFrom(settings);
		modelPerplexity.loadSettingsFrom(settings);
		modelInitDims.loadSettingsFrom(settings);
		modelIterations.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

}
