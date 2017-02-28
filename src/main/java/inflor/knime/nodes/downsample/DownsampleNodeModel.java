package inflor.knime.nodes.downsample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
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
import inflor.core.downsample.DownSample;
import inflor.core.downsample.DownSampleMethods;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * This is the model implementation of Downsample.
 * 
 *
 * @author Aaron Hart
 */
public class DownsampleNodeModel extends NodeModel {

  DownsampleNodeSettings mSettings = new DownsampleNodeSettings();
  private int taskCount = -1;
  private int currentTask;
  private int targetColumnIndex = -1;

  /**
   * Constructor for the node model.
   */
  protected DownsampleNodeModel() {
    super(1,1);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    if (mSettings.getSelectedColumn()==null){
      Arrays.asList(inSpecs[0].getColumnNames())
      .stream()
      .map(inSpecs[0]::getColumnSpec)
      .filter(spec -> spec.getType().equals(FCSFrameFileStoreDataCell.TYPE))
      .findFirst()
      .ifPresent(column -> mSettings.setSelectedColumn(column.getName()));    
    }
    return createSpecs(inSpecs[0]);
  }
  
  private DataTableSpec[] createSpecs(DataTableSpec dataTableSpec) {
    return new DataTableSpec[] {dataTableSpec};
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {
    
    DataTableSpec[] outSpecs = createSpecs(inData[0].getDataTableSpec());
    
    ArrayList<DataRow> data = new ArrayList<>();
    targetColumnIndex = inData[0].getSpec().findColumnIndex(mSettings.getSelectedColumn());
    inData[0].forEach(data::add);

    currentTask = 0;
    taskCount = data.size();
    BufferedDataContainer container = exec.createDataContainer(outSpecs[0]);
    
    data
      .parallelStream()
      .map(row -> createRow(row, exec))
      .forEach(row -> writeRow(row, container, exec));
    
    container.close();
    
    return new BufferedDataTable[]{container.getTable()};
    
  }

  private DataRow createRow(DataRow inRow, ExecutionContext exec){
    final FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) inRow.getCell(targetColumnIndex)).getFCSFrameValue().deepCopy();
    final FCSFrame outFrame = downSample(dataFrame);
    final String fsName = NodeUtilities.getFileStoreName(outFrame);
    final DataCell[] outCells = new DataCell[inRow.getNumCells()];
    synchronized (exec) {
      FileStoreFactory factory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
      FileStore fs;
      FCSFrameFileStoreDataCell fileCell;
      int size = -1;
      try {
        fs = factory.createFileStore(fsName);
        size = NodeUtilities.writeFrameToFilestore(outFrame, fs);
        fileCell = new FCSFrameFileStoreDataCell(fs, outFrame, size);
      } catch (IOException e) {
        getLogger().error("Unable to create file store");
        fs = null;
        fileCell = null;
      }

      for (int j = 0; j < outCells.length; j++) {
        if (j == targetColumnIndex&&size >=0) {
          outCells[j] = fileCell;
        } else  if (j == targetColumnIndex && (size ==-1||fs==null)){
          outCells[j] = new MissingCell("Unable to write filestore.");
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
    }
    return new DefaultRow(inRow.getKey(), outCells);
  }
  
  private FCSFrame downSample(FCSFrame dataFrame) {
    BitSet mask = null;
    List<String> dimensionNames = Arrays.asList(mSettings.getDimensionNames());
    if (mSettings.getSampleMethod().equals(DownSampleMethods.RANDOM)){
      mask = BitSetUtils.getShuffledMask(dataFrame.getRowCount(), mSettings.getCeiling());
    }else if (mSettings.getSampleMethod().equals(DownSampleMethods.DENSITY_DEPENDENT)){
      mask = DownSample.densityDependent(dataFrame, dimensionNames); 
    }
    if (mask!=null){
      return FCSUtilities.filterFrame(mask, dataFrame);
    } else {
      getLogger().error("Downsampling failed due to invalid sample method.");
      return dataFrame;
    }
  }


  private synchronized void writeRow(DataRow row, BufferedDataContainer container, ExecutionContext exec) {
    String message = "Writing: " + ((FCSFrameFileStoreDataCell) row.getCell(targetColumnIndex)).getFCSFrameMetadata().getDisplayName(); 
    exec.setProgress(currentTask/(double)taskCount, message);
    try {
      exec.checkCanceled();
    } catch (CanceledExecutionException e) {
      e.printStackTrace();
    }
    container.addRowToTable(row);
    currentTask++;   
  }
  
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*noop*/}

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
  protected void reset() {/*noop*/}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {/*noop*/}

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
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
  }

}
