package inflor.knime.nodes.compensation.apply;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import com.google.protobuf.InvalidProtocolBufferException;

import inflor.core.compensation.SpilloverCompensator;
import inflor.core.data.FCSFrame;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameDataValue;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;
import inflor.knime.data.type.cell.fcs.FCSFrameMetaData;
import inflor.knime.ports.compensation.CompMatrixPortObject;
import inflor.knime.ports.compensation.CompMatrixPortSpec;

/**
 * This is the model implementation of ApplyCompensation.
 * Attempts to apply a supplied compensation matrix to a dataset.  
 *
 * @author Aaron Hart
 */
public class ApplyCompensationNodeModel extends NodeModel {
      
  public static final String KEY_SELECTED_COLUMN = "Selected Column";
  public static final String KEY_RETAIN_UNCOMPED = "Retain Uncomped Dimensions";

  public static final boolean DEFAULT_RETAIN_UNCOMPED = false;
  public static final String DEFAULT_SELECTED_COLUMN = "Select...";
  
  private SettingsModelColumnName mSelectedColumn=  new SettingsModelColumnName(KEY_SELECTED_COLUMN, DEFAULT_SELECTED_COLUMN);
  private SettingsModelBoolean    mRetainUncomped = new SettingsModelBoolean(KEY_RETAIN_UNCOMPED, DEFAULT_RETAIN_UNCOMPED);

  private int progress;

  private long fileCount;

  private CopyOnWriteArrayList<FCSFrame> summaryData;
  
    /**
     * Constructor for the node model.
     */
    protected ApplyCompensationNodeModel() {
    
      super(new PortType[] {PortTypeRegistry.getInstance().getPortType(CompMatrixPortObject.class),
                            PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class)},
            
            new PortType[] {PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class)});
    }

    /**
     * {@inheritDoc}
     * @throws IOException 
     * @throws CanceledExecutionException 
     */
    
    @Override
    protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws CanceledExecutionException{
        
      exec.setMessage("Beginning Execution.");
      exec.setProgress(0.001);
      // Create the output spec and data container.
      DataTableSpec outSpec;
      try {
        outSpec = createSpec(new PortObjectSpec[]{inObjects[0].getSpec(), inObjects[1].getSpec()});
      } catch (NotConfigurableException e1) {
        // TODO Auto-generated catch block
        throw new CanceledExecutionException(e1.getMessage());
      }
      BufferedDataContainer container = exec.createDataContainer(outSpec);
      String columnName = mSelectedColumn.getColumnName();
      int columnIndex = outSpec.findColumnIndex(columnName);
      CompMatrixPortObject cmpo = (CompMatrixPortObject) inObjects[0];
      CompMatrixPortSpec cmSpec = (CompMatrixPortSpec) cmpo.getSpec();
      SpilloverCompensator compr = new SpilloverCompensator(cmSpec.getInputDimensions(), cmSpec.getOutputDimensions(),cmpo.getSpilloverValues());
      
      BufferedDataTable inTable = (BufferedDataTable) inObjects[1];
      List<DataRow> inCells = new ArrayList<>();
      
      
      progress = 0;
      fileCount = inTable.size();
      for (final DataRow inRow : inTable) {
        inCells.add(inRow);
      }
      summaryData = new CopyOnWriteArrayList<>();
      try{
        inCells
          .parallelStream()
          .forEach(inRow -> processRow(container, columnIndex, compr, inRow, exec));
      } catch (RuntimeException e){
        throw new CanceledExecutionException("Execution cancelled: " + e.getMessage());
      }
      container.close();
      
      FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(summaryData, Integer.MAX_VALUE);
      
      String key = NodeUtilities.PREVIEW_FRAME_KEY;
      String value = summaryFrame.saveAsString();
      Map<String, String> content = new HashMap<>();
      content.put(key, value);
      BufferedDataTable finalTable = NodeUtilities.addPropertyToColumn(exec, container.getTable(), columnName, content);
      
      return new BufferedDataTable[] {finalTable};
    }

    private void processRow(BufferedDataContainer container, int columnIndex, SpilloverCompensator compr,
        DataRow inRow, ExecutionContext exec) {
      //Do the stuff we can in parallel.
      FCSFrame inFrame = ((FCSFrameFileStoreDataCell) inRow.getCell(columnIndex)).getFCSFrameValue();
      FCSFrame compFrame;
      try {
        compFrame = compr.compensateFCSFrame(inFrame, mRetainUncomped.getBooleanValue());
      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException("Invalid protocol message.", e);
      }
      
      int downSize = (int) (FCSUtilities.DEFAULT_MAX_SUMMARY_FRAME_VALUES/fileCount/compFrame.getDimensionCount());
      BitSet mask = BitSetUtils.getShuffledMask(compFrame.getRowCount(), downSize);
      FCSFrame filterFrame = FCSUtilities.filterFrame(mask, compFrame);
      summaryData.add(filterFrame);
      //Synchronize this for writing rows.
      writeRow(container, columnIndex, inRow, compFrame, exec);
    }

    private synchronized void writeRow(BufferedDataContainer container, int index, DataRow inRow, FCSFrame df, ExecutionContext exec) {
      final FileStoreFactory fsf = FileStoreFactory.createWorkflowFileStoreFactory(exec);
      
      progress++;
      exec.setMessage("Writing: " + df.getDisplayName());
      exec.setProgress(progress/fileCount);
      try {
        exec.checkCanceled();
      } catch (CanceledExecutionException e1) {
        throw new RuntimeException("User cancelled execution.");
      }
      
      FileStore fs;
      try {
        fs = fsf.createFileStore(df.getDisplayName() +" "+ df.getID());
      } catch (IOException e) {
        throw new RuntimeException("IO Exception while creating file store.", e);
      }
      
      int messageSize = NodeUtilities.writeFrameToFilestore(df, fs);
      FCSFrameMetaData metaData = new FCSFrameMetaData(df, messageSize);
      
      FCSFrameFileStoreDataCell fileCell = new FCSFrameFileStoreDataCell(fs, metaData);
      DataCell[] outCells = new DataCell[inRow.getNumCells()];
      for (int j = 0; j < outCells.length; j++) {
        if (j == index) {
          outCells[j] = fileCell;
        } else {
          outCells[j] = inRow.getCell(j);
        }
      }
      RowKey key = new RowKey(inRow.getKey().getString());
      DataRow outRow = new DefaultRow(key, outCells);
      container.addRowToTable(outRow);
    }

    private DataTableSpec createSpec(PortObjectSpec[] inSpecs) throws NotConfigurableException {
      CompMatrixPortSpec matrixSpec = (CompMatrixPortSpec) inSpecs[0];
      DataTableSpec tableSpec = (DataTableSpec) inSpecs[1];
      String columnName = mSelectedColumn.getColumnName();
      DataColumnSpecCreator creator = new DataColumnSpecCreator(columnName, FCSFrameFileStoreDataCell.TYPE);

      DataColumnProperties newProps = updateColumnProperties(matrixSpec, tableSpec, columnName);
      
      creator.setProperties(newProps);
      DataColumnSpec newSpec = creator.createSpec();
      DataColumnSpec[] colSpecs = new DataColumnSpec[tableSpec.getColumnNames().length];
      for (int i=0;i<colSpecs.length;i++){
        DataColumnSpec currentSpec = tableSpec.getColumnSpec(i);
        if (currentSpec.getName().equals(columnName)){
          colSpecs[i] = newSpec;
        } else {
          colSpecs[i] = tableSpec.getColumnSpec(i);
        }
      }
      return new DataTableSpec(colSpecs);
    }

    private DataColumnProperties updateColumnProperties(CompMatrixPortSpec matrixSpec,
        DataTableSpec tableSpec, String columnName) throws NotConfigurableException {
      DataColumnSpec selectedColSpec = tableSpec.getColumnSpec(columnName);
      DataColumnProperties properties = selectedColSpec.getProperties();
      
      HashMap<String, String> newProperties = new HashMap<>();
      //old dimension names
      String dimensionNameString = properties.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
      String displayNameString = properties.getProperty(NodeUtilities.DISPLAY_NAMES_KEY);
      
      if (dimensionNameString!=null&&displayNameString!=null){
        String[] oldDimensionNames = dimensionNameString.split(NodeUtilities.DELIMITER_REGEX);
        String[] oldDisplayNames = displayNameString.split(NodeUtilities.DELIMITER_REGEX);
        String[] inCompNames = matrixSpec.getInputDimensions();
        ArrayList<String> newShortNames = new ArrayList<>();
        ArrayList<String> newDisplayNames = new ArrayList<>();
        
        //for each input dimension name
        for (int i=0;i<oldDimensionNames.length;i++){
          int inCompIndex = Arrays.asList(inCompNames).indexOf(oldDimensionNames[i]);
          //if it isn't in the list of dimensions to compensate, add it to the list of output dimensions
          if (inCompIndex==-1){
            newShortNames.add(oldDimensionNames[i]);
            newDisplayNames.add(oldDisplayNames[i]);
          //Otherwise, if we keep uncompensated parameters, add the . 
          } else {
            newShortNames.add(FCSUtilities.formatCompStainName(oldDimensionNames[i]));
            newDisplayNames.add(FCSUtilities.formatCompStainName(oldDisplayNames[i]));
          }
        }
        
        if (mRetainUncomped.getBooleanValue()){
          //now add uncomped dimension names if desired
          for (int i=0;i<inCompNames.length;i++){
            newShortNames.add(inCompNames[i]);
            int dimIndex = Arrays.asList(oldDimensionNames).indexOf(inCompNames[i]);
            newDisplayNames.add(oldDisplayNames[dimIndex]);
          }
        }
        
        String shortNamesString = String.join(NodeUtilities.DELIMITER, newShortNames);
        newProperties.put(NodeUtilities.DIMENSION_NAMES_KEY, shortNamesString);
        
        String displayNamesString = String.join(NodeUtilities.DELIMITER, newDisplayNames);
        newProperties.put(NodeUtilities.DISPLAY_NAMES_KEY, displayNamesString);
        
        return properties.cloneAndOverwrite(newProperties);
      } else {
        throw new NotConfigurableException("Unable to find dimension names: Execute reader?");
      }
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
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
      try {
        //identify any compatible FCS Columns
        if (mSelectedColumn.getColumnName().equals(DEFAULT_SELECTED_COLUMN)){
          DataTableSpec dataTableSpec = (DataTableSpec) inSpecs[1];
          dataTableSpec.containsCompatibleType(FCSFrameDataValue.class);
          for (int i=0;i<dataTableSpec.getNumColumns();i++){
            if (dataTableSpec.getColumnSpec(i).getType().equals(FCSFrameFileStoreDataCell.TYPE)){
              mSelectedColumn.setSelection(dataTableSpec.getColumnSpec(i).getName(), false);
              break;
            }
          }
        }
        return new PortObjectSpec[]{createSpec(inSpecs)};
      } catch (NotConfigurableException e) {
        throw new InvalidSettingsException(e.getMessage());
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         mRetainUncomped.saveSettingsTo(settings);
         mSelectedColumn.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {    
      mRetainUncomped.loadSettingsFrom(settings);
      mSelectedColumn.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {/*TODO*/}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {/*TODO*/}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {/*TODO*/}
}