package main.java.inflor.knime.nodes.utility.extractsubset;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.BitSetUtils;
import main.java.inflor.knime.core.NodeUtilities;
import main.java.inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;


/**
 * This is the model implementation of ExtractTrainingSet.
 * Extracts data from an FCS frame column to a standard KNIME Table. Data may be transformed and gating information included for downstream ML applications.
 *
 * @author Aaron Hart
 */
public class ExtractTrainingSetNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(ExtractTrainingSetNodeModel.class);

	static final String KEY_PERFILE_EVENT_COUNT = "Count";
    static final int DEFAULT_COUNT = 5000;

    static final int MIN_COUNT = 0;
    static final int MAX_COUNT = Integer.MAX_VALUE;

    static final String KEY_SELECTED_COLUMN = "Selected column";
    static final String DEFAULT_COLUMN = "none";
    
    private SettingsModelColumnName mSelectedColumn = new SettingsModelColumnName(KEY_SELECTED_COLUMN, DEFAULT_COLUMN);
    private SettingsModelIntegerBounded mCount = new SettingsModelIntegerBounded(
        KEY_PERFILE_EVENT_COUNT,
        DEFAULT_COUNT,
        MIN_COUNT, MAX_COUNT);

    /**
     * Constructor for the node model.
     */
    protected ExtractTrainingSetNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("ExtractTrainingSetNodeModel executing.");

        DataTableSpec outputSpec = createSpec(inData[0].getSpec());
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        int frameIndex = inData[0].getSpec().findColumnIndex(mSelectedColumn.getStringValue());
        int rowIndex = -1;
        for (DataRow inRow: inData[0]){
          FCSFrameFileStoreDataCell cell = (FCSFrameFileStoreDataCell) inRow.getCell(frameIndex);
          FCSFrame dataFrame = cell.getFCSFrameValue();
          if (rowIndex%1000 == 0){
            exec.setProgress((double) rowIndex/(inData[0].size()*mCount.getIntValue()));
            exec.setMessage("Down sampling: " + dataFrame.getPrefferedName());
          }
          rowIndex = writeRow(container, rowIndex, dataFrame);          
        }
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }

    private int writeRow(BufferedDataContainer container, int rowIndex, FCSFrame dataFrame) {
      BitSet mask = BitSetUtils.getShuffledMask(dataFrame.getRowCount(), mCount.getIntValue());
      
      DataCell[] cells;
      for (int i=0;i<mask.length();i++){
        if (mask.get(i)){
          if (dataFrame.getSubsets().isEmpty()){
            double[] dimensionValues = dataFrame.getDimensionRow(i);
            cells = new DataCell[dimensionValues.length + 1];
            for (int j=0;j<dimensionValues.length;j++){
              cells[j] = new DoubleCell(dimensionValues[j]);
            }
          } else {
            double[] dimensionValues = dataFrame.getDimensionRow(i);
            String[] subsetValues = dataFrame.getSubsetRow(i);
            cells = new DataCell[dimensionValues.length + subsetValues.length + 1];
            for (int j=0;j<dimensionValues.length;j++){
              cells[j] = new DoubleCell(dimensionValues[j]);
            }
            for (int k=0;k<subsetValues.length;k++){
              cells [dimensionValues.length + k ] = new StringCell(subsetValues[k]);
            }
          }

          cells[cells.length-1] = new StringCell(dataFrame.getPrefferedName());
          final RowKey rowKey = new RowKey("Row " + rowIndex++);
          final DataRow tableRow = new DefaultRow(rowKey, cells);
          container.addRowToTable(tableRow);
        }
      }
      return rowIndex;
    }

    private DataTableSpec createSpec(DataTableSpec inSpec) {
      String columnName = mSelectedColumn.getColumnName();
      DataColumnProperties properties = inSpec.getColumnSpec(columnName).getProperties();
      String rawDimensionNames = properties.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
      String[] dimensionNames = rawDimensionNames.split(NodeUtilities.DELIMITER_REGEX);
      
      DataColumnSpec[] colSpecs;
      int outColumnCount;

      if (properties.containsProperty(NodeUtilities.SUBSET_NAMES_KEY)){
        String rawSubsetNames = properties.getProperty(NodeUtilities.SUBSET_NAMES_KEY);
        String[] subsetNames = rawSubsetNames.split(NodeUtilities.DELIMITER_REGEX);
        outColumnCount = subsetNames.length + dimensionNames.length + 1;
        colSpecs = new DataColumnSpec[outColumnCount];
        for (int i=0;i<dimensionNames.length;i++){
          colSpecs[i] = new DataColumnSpecCreator(dimensionNames[i], DoubleCell.TYPE).createSpec();
        }
        
        for (int i = 0;i<subsetNames.length;i++){
          colSpecs[i + dimensionNames.length] = new DataColumnSpecCreator(subsetNames[i], StringCell.TYPE).createSpec();
        }
      } else {
        outColumnCount = dimensionNames.length + 1;
        colSpecs = new DataColumnSpec[outColumnCount];
        for (int i=0;i<dimensionNames.length;i++){
          colSpecs[i] = new DataColumnSpecCreator(dimensionNames[i], DoubleCell.TYPE).createSpec();
        }
      }
      
      colSpecs[outColumnCount-1] = new DataColumnSpecCreator("Source", StringCell.TYPE).createSpec();
      
      return new DataTableSpec(colSpecs);
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
      DataTableSpec inSpec = inSpecs[0];
      if (mSelectedColumn.getColumnName() == DEFAULT_COLUMN){
        for (int i=0;i<inSpec.getNumColumns();i++){
          inSpec.getColumnSpec(i).getType().equals(FCSFrameFileStoreDataCell.TYPE);
          mSelectedColumn.setSelection(inSpec.getName(), false);
        }
      }
      DataTableSpec outSpec = createSpec(inSpec);
      return new DataTableSpec[]{outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {        
        mCount.saveSettingsTo(settings);
        mSelectedColumn.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        mCount.loadSettingsFrom(settings);
        mSelectedColumn.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        mCount.validateSettings(settings);
        mSelectedColumn.validateSettings(settings);
    }
    
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