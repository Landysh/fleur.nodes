package inflor.knime.nodes.treesne;

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

import inflor.core.sne.tsne.barneshut.BHTSne2;
import inflor.core.sne.utils.MatrixOps;

/**
 * This is the model implementation of the TSNE node for the KNIME Analytics Platform. Calculates a
 * tSNE using library developed by Leif Jonsson: https://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TreeSNENodeModel extends NodeModel {

  TreeSNENodeSettings mSettings = new TreeSNENodeSettings();

  private ArrayList<SNEIterationBean> resultList;

  /**
   * Constructor for the node model.
   */
  protected TreeSNENodeModel() {

    super(1, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpec)
      throws InvalidSettingsException {
    final DataTableSpec outSpec =  createTableSpec(inSpec[0]);
    return new DataTableSpec[] {outSpec};
  }

  private DataTableSpec createTableSpec(DataTableSpec inSpec) {
    String[] dimensionNames = mSettings.getSelectedDimension();
    
    DataColumnSpec[] colSpecs = new DataColumnSpec[dimensionNames.length+4];
    for (int i=0;i<dimensionNames.length;i++){
      colSpecs[i] = new DataColumnSpecCreator(dimensionNames[i], DoubleCell.TYPE).createSpec();
    }
    
    colSpecs[colSpecs.length-4] = new DataColumnSpecCreator(TreeSNENodeSettings.TSNE_1, DoubleCell.TYPE).createSpec();
    colSpecs[colSpecs.length-3] = new DataColumnSpecCreator(TreeSNENodeSettings.TSNE_2, DoubleCell.TYPE).createSpec();
    colSpecs[colSpecs.length-2] = new DataColumnSpecCreator(TreeSNENodeSettings.TREESNE_1, DoubleCell.TYPE).createSpec();
    colSpecs[colSpecs.length-1] = new DataColumnSpecCreator(TreeSNENodeSettings.TREESNE_2, DoubleCell.TYPE).createSpec();

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
    bht.init(data, n, d, 2, mSettings.getPCADims(), mSettings.getPerplexity(), mSettings.getMaxIterations(), true, 0.5);
    exec.setProgress(0.15);
    exec.setMessage("Main Loop: ");
    ExecutionContext iterExec = exec.createSubExecutionContext(1);
    resultList = new ArrayList<>();
    boolean keepGoing = true;
    while (keepGoing) {
      double num = bht.getCurrentIteration();
      double den = bht.getMaxIterations();
      iterExec.setProgress(num / den);
      iterExec.setMessage("Iteration: " + num);
      iterExec.checkCanceled();
      double[][] yCurrent = bht.runInteractively();
      if (yCurrent[0].length == 0) {
        keepGoing = false;
      } else {
        keepGoing = true;
        resultList.add(new SNEIterationBean((int) num, yCurrent));
      }
    }
    final double[][] yFinal = resultList.get(resultList.size() - 1).getData();

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
      throws IOException, CanceledExecutionException {/* TODO */}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    mSettings.load(settings);
  }

  private double[][] readData(BufferedDataTable inData, ExecutionContext exec) {
    final long rowCount = inData.size();
    final String[] columns = mSettings.getSelectedDimension();
    final double[][] dataTable = new double[(int) rowCount][columns.length];
    int i = 0;
    for (final DataRow inRow : inData) {
      exec.setProgress((double) i / inData.size() / 10);
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
  protected void reset() {/* TODO */}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/* TODO */}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    mSettings.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings)
      throws InvalidSettingsException {/* TODO */}
}
