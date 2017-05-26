package inflor.knime.nodes.sne;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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

import inflor.core.data.FCSFrame;
import inflor.core.regressiontree.MultiTargetRegressionTree;
import inflor.core.regressiontree.RegressionTreeNode.LeafStats;
import inflor.core.sne.tsne.barneshut.BHTSne2;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSConcatenator;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.MatrixUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of the TSNE node for the KNIME Analytics Platform. Calculates a
 * tSNE using library developed by Leif Jonsson: https://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TreeSNENodeModel extends NodeModel {

  private static final int OUT_DIMENSION_COUNT = 2;

  private static final double DEFAULT_THETA = 0.5;

  TreeSNENodeSettings mSettings = new TreeSNENodeSettings();

  private ArrayList<SNEIterationBean> resultList;

  private TransformSet transformSet;

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
    
    DataColumnSpec[] colSpecs = new DataColumnSpec[dimensionNames.length+2];
    for (int i=0;i<dimensionNames.length;i++){
      colSpecs[i] = new DataColumnSpecCreator(dimensionNames[i], DoubleCell.TYPE).createSpec();
    }
    
    colSpecs[colSpecs.length-4] = new DataColumnSpecCreator(mSettings.TSNE_1, DoubleCell.TYPE).createSpec();
    colSpecs[colSpecs.length-3] = new DataColumnSpecCreator(mSettings.TSNE_2, DoubleCell.TYPE).createSpec();

    return new DataTableSpec(colSpecs);
  }
  
  private FCSFrame readFrame(DataRow row, int index){
    if (row.getCell(index) instanceof FCSFrameFileStoreDataCell){
      return ((FCSFrameFileStoreDataCell)row.getCell(index)).getFCSFrameValue();
    } else {
      throw new RuntimeException("Referenced data cell is not valid.");
    }
  }
  
  private FCSFrame downSample(FCSFrame df, int size){
    BitSet mask = BitSetUtils.getShuffledMask(df.getRowCount(), size);
    return FCSUtilities.filterFrame(mask, df);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {

    //Get file references
    exec.setProgress(0.01);
    exec.checkCanceled();
    ArrayList<DataRow> rowData = new ArrayList<>();
    CloseableRowIterator it = inData[0].iterator();
    while(it.hasNext()){
      rowData.add(it.next());
    }
    //Create the tsne matrix. 
    exec.setProgress(0.02);
    exec.setMessage("Creating training data.");
    exec.checkCanceled();
    int perFileObs = (int) (mSettings.getMaxObservations()/inData[0].size());
    int fcsColumnIndex = inData[0].getSpec().findColumnIndex(mSettings.getSelectedColumn());
    
    FCSConcatenator acc = new FCSConcatenator();
    
    Optional<FCSFrame> trainingFrame = rowData
      .parallelStream()
      .map(row -> readFrame(row, fcsColumnIndex))
      .map(frame -> downSample(frame, perFileObs))
      .reduce(acc);
    
    if (!trainingFrame.isPresent()){
      throw new CanceledExecutionException("Unable to create training set.");
    }
    
    String[] dimensionNames = mSettings.getSelectedDimension();
    double[][] X = trainingFrame.get().getMatrix(dimensionNames);
       
    final String columnName = mSettings.getSelectedColumn();    
    DataColumnProperties props = inData[0].getSpec().getColumnSpec(columnName).getProperties();
    if (props.containsProperty(NodeUtilities.KEY_TRANSFORM_MAP)){
        transformSet = TransformSet.loadFromProtoString(props.getProperty(NodeUtilities.KEY_TRANSFORM_MAP));
    } else {
        transformSet = new TransformSet();
    }
    MatrixUtilities.transformMatrix(dimensionNames, transformSet, X);
    MatrixUtilities.centerAndScale(X);
    double[][] rowX = MatrixUtilities.transpose(X);

    //Run tsne
    BHTSne2 bht = new BHTSne2();
    int n = rowX.length;
    int d = rowX[0].length;
    int outDims = OUT_DIMENSION_COUNT;
    int pcaDims = mSettings.getPCADims();
    int perplexity = mSettings.getPerplexity();
    int iterations = mSettings.getMaxIterations();
    boolean usePCA = true;
    double theta = DEFAULT_THETA;
    
    bht.init(rowX, n, d, outDims, pcaDims, perplexity, iterations, usePCA, theta);
    exec.setProgress(0.15);
    exec.setMessage("Main Loop: ");
    ExecutionContext iterExec = exec.createSubExecutionContext(1);
    resultList = new ArrayList<>();
    boolean keepGoing = true;
    while (keepGoing) {
      double num = bht.getCurrentIteration();
      double den = bht.getMaxIterations();
      iterExec.setProgress(num/den);
      iterExec.setMessage("Iteration: "+num);
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

    //train the regression tree
    HashMap<String, double[]> xMap = new HashMap<>();
    HashMap<String, double[]> yMap = new HashMap<>();
    HashMap<String, double[]> treeMSDs = new HashMap<>();
    for (int i=0;i<X.length;i++){
      treeMSDs.put(dimensionNames[i], X[i]);
      xMap.put(mSettings.getSelectedDimension()[i], X[i]);
    }
    double[][] colY = MatrixUtilities.transpose(yFinal);
    yMap.put(mSettings.TSNE_1, colY[0]);
    yMap.put(mSettings.TSNE_2, colY[1]);

    MultiTargetRegressionTree tree = new MultiTargetRegressionTree(xMap, yMap);
    boolean finishedLearning = false;
    int nodes=0;
    while(!finishedLearning){
      finishedLearning = tree.learn();
      if (nodes%10 == 0){
        exec.checkCanceled();
        exec.setMessage("splitting node: " + nodes);
      }
      nodes++;
    }
    BufferedDataContainer container = exec.createDataContainer(createTableSpec(inData[0].getSpec()));
    //Write the results.
    DataCell[] cells = new DataCell[rowX[0].length+yFinal[0].length+2];
    for (int i=0;i<yFinal.length;i++){
      for (int j=0;j<rowX[i].length;j++){
        cells[j] = new DoubleCell(rowX[i][j]);
      }
      for (int k=0;k<yFinal[i].length;k++){
        cells[rowX[0].length+k] = new DoubleCell(yFinal[i][k]);
      }
      exec.checkCanceled();
      exec.setProgress(i/rowX.length);
      LeafStats results = tree.predict(rowX[i]);
      Double ts1 = results.yStats.get(mSettings.TSNE_1)[LeafStats.INDEX_MEAN];
      Double ts2 = results.yStats.get(mSettings.TSNE_2)[LeafStats.INDEX_MEAN];
      cells[cells.length-2] = new DoubleCell(ts1);
      cells[cells.length-1] = new DoubleCell(ts2);
      DataRow row = new DefaultRow("Row " + i, cells);
      container.addRowToTable(row);
    }
    //score the training data
    
    //write the output table. 
    
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
