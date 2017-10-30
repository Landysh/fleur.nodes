package inflor.knime.nodes.bhtsne.table;

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

import inflor.core.sne.tsne.barneshut.BHTSne2;
import inflor.core.sne.utils.MatrixOps;

/**
 * This is the model implementation of the TSNE node for the KNIME Analytics Platform. Calculates a
 * tSNE using library developed by Leif Jonsson: https://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNENodeModel extends NodeModel {

  // Column filter
  static final String KEY_SLECTED_COLUMNS = "Columns";

  // Iterations
  static final String KEY_ITERATIONS = "Iterations";

  static final Integer MIN_ITERATIONS = 10;
  static final Integer MAX_ITERATIONS = Integer.MAX_VALUE;
  static final Integer DEFAULT_ITERATIONS = 500;
  // PCA Dims
  static final String KEY_PCA_DIMS = "PCA Dimensions";
  static final Integer MIN_PCA_DIMS = 0;
  static final Integer MAX_PCA_DIMS = Integer.MAX_VALUE;
  static final Integer DEFAULT_PCA_DIMS = 10;
  // Perplexity
  static final String KEY_PERPLEXITY = "Maximum iterations";
  static final Double MIN_PERPLEXITY = 1.;
  static final Double MAX_PERPLEXITY = Double.MAX_VALUE;
  static final Double DEFAULT_PERPLEXITY = 20.;
  
  private final SettingsModelColumnFilter2 modelColumns =
      new SettingsModelColumnFilter2(KEY_SLECTED_COLUMNS);
  private final SettingsModelIntegerBounded modelIterations = new SettingsModelIntegerBounded(
      KEY_ITERATIONS, DEFAULT_ITERATIONS, MIN_ITERATIONS, MAX_ITERATIONS);
  private final SettingsModelIntegerBounded modelInitDims = new SettingsModelIntegerBounded(
      KEY_PCA_DIMS, DEFAULT_PCA_DIMS, MIN_PCA_DIMS, MAX_PCA_DIMS);

  private final SettingsModelDoubleBounded modelPerplexity = new SettingsModelDoubleBounded(
      KEY_PERPLEXITY, DEFAULT_PERPLEXITY, MIN_PERPLEXITY, MAX_PERPLEXITY);

private ArrayList<SNEIterationBean> resultList;

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
  protected DataTableSpec[] configure(final DataTableSpec[] inSpec)
      throws InvalidSettingsException {
    final DataTableSpec newColSpec = createTableSpec(inSpec[0]);
    final DataTableSpec outSpec = new DataTableSpec(inSpec[0], newColSpec);
    return new DataTableSpec[] {outSpec};
  }

  private DataTableSpec createTableSpec(DataTableSpec inSpec) {
    DataColumnSpec[] colSpecs = new DataColumnSpec[2];
    
    StringBuilder tSNE1Name = new StringBuilder("TSNE1");
    while (inSpec.containsName(tSNE1Name.toString())){
      tSNE1Name.append("*");
    }
    colSpecs[0] = new DataColumnSpecCreator(tSNE1Name.toString(), DoubleCell.TYPE).createSpec();
    
    StringBuilder tSNE2Name = new StringBuilder("TSNE2");
    while (inSpec.containsName(tSNE2Name.toString())){
      tSNE2Name.append("*");
    }
    colSpecs[1] = new DataColumnSpecCreator(tSNE2Name.toString(), DoubleCell.TYPE).createSpec();
    
    return new DataTableSpec(colSpecs);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {
    
    exec.setProgress(0.01);
    exec.setMessage("Data read");
    double[][] data = readData(inData[0], exec);
    exec.setProgress(0.05);
    exec.setMessage("Scaling data.");
    data = MatrixOps.centerAndScale(data);
    exec.setProgress(0.1);
    exec.setMessage("Initializing bhtSNE");
    BHTSne2 bht = new BHTSne2();
    int n = data.length;
    int d = data[0].length;
    bht.init(data, n, d, 2, modelInitDims.getIntValue(), modelPerplexity.getDoubleValue(), modelIterations.getIntValue(), true, 0.5);
    exec.setProgress(0.15);
    exec.setMessage("Main Loop: ");
    ExecutionContext iterExec = exec.createSubExecutionContext(1);
    resultList = new ArrayList<>();
    boolean keepGoing = true;
    while (keepGoing){
      double num = bht.getCurrentIteration();
      double den = bht.getMaxIterations();
      iterExec.setProgress(num/den);
      iterExec.setMessage("Iteration: " + num);
      iterExec.checkCanceled();
      double[][] yCurrent = bht.runInteractively();
      if (yCurrent[0].length==0){
        keepGoing = false;
      } else {
        keepGoing = true;
        resultList.add(new SNEIterationBean((int) num, yCurrent));
      }
    }   
    final double[][] yFinal = resultList.get(resultList.size()-1).getData();

    final DataTableSpec newColSpec = createTableSpec(inData[0].getSpec());
    final DataTableSpec spec = new DataTableSpec(inData[0].getSpec(), newColSpec);

    final BufferedDataContainer container = exec.createDataContainer(spec);
    final CloseableRowIterator rowIterator = inData[0].iterator();

    long rowCount = 0;
    while (rowIterator.hasNext()) {
      final RowKey rowKey = new RowKey("Row " + rowCount);
      final DataRow inCols = rowIterator.next();
      final double[] tsneCols = yFinal[(int) rowCount];
      final DoubleCell tsne1 = new DoubleCell(tsneCols[0]);
      final DoubleCell tsne2 = new DoubleCell(tsneCols[1]);
      final DataCell[] tsneCells = new DataCell[] {tsne1, tsne2};
      final DataRow tsneRow = new DefaultRow(rowKey, tsneCells);
      final ArrayList<DataCell> cells =
          new ArrayList<>(inCols.getNumCells() + tsneRow.getNumCells());
      for (final DataCell cell : inCols) {
        cells.add(cell);
      }
      for (final DataCell cell : tsneRow) {
        cells.add(cell);
      }
      final DataRow outRow = new DefaultRow(rowKey, cells);
      container.addRowToTable(outRow);
      rowCount++;
    }
    container.close();
    return new BufferedDataTable[] {container.getTable()};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*TODO*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    modelColumns.loadSettingsFrom(settings);
    modelPerplexity.loadSettingsFrom(settings);
    modelInitDims.loadSettingsFrom(settings);
    modelIterations.loadSettingsFrom(settings);
  }

  private double[][] readData(BufferedDataTable inData, ExecutionContext exec) {
    final long rowCount = inData.size();
    final String[] columns = modelColumns.applyTo(inData.getSpec()).getIncludes();
    final double[][] dataTable = new double[(int) rowCount][columns.length];
    int i = 0;
    for (final DataRow inRow : inData) {
      exec.setProgress((double)i/inData.size()/10);
      final double[] outRow = new double[columns.length];
      for (int j = 0; j < columns.length; j++) {
        final int specIndex = inData.getSpec().findColumnIndex(columns[j]);
        final DataCell cell = inRow.getCell(specIndex);
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
    modelColumns.saveSettingsTo(settings);
    modelIterations.saveSettingsTo(settings);
    modelPerplexity.saveSettingsTo(settings);
    modelInitDims.saveSettingsTo(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {/*TODO*/}
}