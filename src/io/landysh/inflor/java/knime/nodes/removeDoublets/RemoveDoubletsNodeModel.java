package io.landysh.inflor.java.knime.nodes.removeDoublets;

import java.io.File;
import java.io.IOException;

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

import io.landysh.inflor.java.core.ColumnStore;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.nodes.readFCSFool.ReadFCSSetNodeModel;

/**
 * This is the model implementation of RemoveDoublets.
 * 
 *
 * @author Landysh Co.
 */
public class RemoveDoubletsNodeModel extends NodeModel {
    
	RemoveDoubletsSettingsModel m_settings = new RemoveDoubletsSettingsModel();
	
    private static final NodeLogger logger = NodeLogger
            .getLogger(ReadFCSSetNodeModel.class);
	
    /**
     * Constructor for the node model.
     */
    protected RemoveDoubletsNodeModel() { 
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Beginning Execution.");
		FileStoreFactory fileStoreFactory 	= FileStoreFactory.createWorkflowFileStoreFactory(exec);

        // Create the output spec and data container.
        DataTableSpec outSpec = createSpec(inData[0].getDataTableSpec());
        BufferedDataContainer container = exec.createDataContainer(outSpec);
    	String columnName = m_settings.getSelectedColumnSettingsModel().getStringValue();
		int index = outSpec.findColumnIndex(columnName);
        String areaColumn = m_settings.getAreaColumnSettingsModel().getStringValue();
        String heightColumn = m_settings.getHeightColumnSettingsModel().getStringValue();
        
        int i=0;
		for (DataRow inRow:inData[0]){
			DataCell[] outCells = new DataCell[inRow.getNumCells()];			
			ColumnStore columnStore = ((ColumnStoreCell)inRow.getCell(index)).getColumnStore();
        	SingletsModel model = new SingletsModel(columnStore.getColumnNames());
        	double[] areaData = columnStore.getColumn(areaColumn);
        	double[] heightData = columnStore.getColumn(heightColumn);
        	double[] ratio = model.buildModel(areaData, heightData);
        	boolean[] mask = model.scoreModel(ratio);
        	
        	// now create the output row
    		ColumnStore outStore = new ColumnStore(columnStore.getKeywords(), columnStore.getColumnNames());
    		for (String name:columnStore.getColumnNames()){
    			double[] maskedColumn = FCSUtils.getMaskColumn(mask, columnStore.getColumn(name));
    			outStore.addColumn(name, maskedColumn);
    		}
       		String fsName = i + "ColumnStore.fs";
    		FileStore fileStore = fileStoreFactory.createFileStore(fsName);
    		ColumnStoreCell fileCell= new ColumnStoreCell(fileStore, columnStore);

    		for (int j=0;j<outCells.length;j++){
    			if (j==index){
    				outCells[j] = fileCell;
    			} else {
    				outCells[j] = inRow.getCell(j);
    			}
    		}
			DataRow outRow = new DefaultRow("Row " + i, outCells);
			container.addRowToTable(outRow);
    		i++;
        }
		container.close();
        return new BufferedDataTable[]{container.getTable()};
    }

    private DataTableSpec createSpec(DataTableSpec dataTableSpec) {
		return dataTableSpec;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	DataTableSpec spec = createSpec(inSpecs[0]);
        return new DataTableSpec[]{spec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.save(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings.load(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings.validate(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
}

