package io.landysh.inflor.java.knime.nodes.portToTableCell;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import io.landysh.inflor.java.core.ColumnStore;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;
import io.landysh.inflor.java.knime.views.CellLineageRenderer;


/**
 * This is the model implementation of ColumnStoreToTableCell.
 * Converts a 
 *
 * @author Landysh Co.
 */
public class ColumnStoreToTableCellNodeModel extends NodeModel {
    
	/**
     * Constructor for the node model.
     */
    protected ColumnStoreToTableCellNodeModel() {
           super(new PortType[]{ColumnStorePortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     * @throws CanceledExecutionException 
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws CanceledExecutionException {
		//create the output container
    	DataColumnSpec colSpecs = new DataColumnSpecCreator("Listmode Data", ColumnStoreCell.TYPE).createSpec();
		DataTableSpec spec = new DataTableSpec(colSpecs );
		BufferedDataContainer container = exec.createDataContainer(spec);
				
		//Create the file store
		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
		FileStore fs;
		try {
			fs = fileStoreFactory.createFileStore("column.store");
		} catch (IOException e) {
			e.printStackTrace();
			throw new CanceledExecutionException("Unable to create FileStore, cancelling execution.");
		}
		
		//get the data and write it to the container
		ColumnStore cs = ((ColumnStorePortObject) inData[0]).getColumnStore();
		DataCell[] dataCells = new DataCell[]{new ColumnStoreCell(fs, cs)};
		DataRow dataRow = new DefaultRow("Row 0", dataCells);
		container.addRowToTable(dataRow);
		
		//cleanup and create the table
		container.close();
		BufferedDataTable table = container.getTable();
		return new BufferedDataTable[]{table};
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
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
		DataColumnSpecCreator colSpec = new DataColumnSpecCreator("Listmode Data", ColumnStoreCell.TYPE);
		colSpec.setProperties(new DataColumnProperties(
		          Collections.singletonMap(
		                  DataValueRenderer.PROPERTY_PREFERRED_RENDERER, 
		                  CellLineageRenderer.DESCRIPTION)));
		org.knime.core.data.DataTableSpec spec = new DataTableSpec(colSpec.createSpec());
        return new DataTableSpec[]{spec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {}
    
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
            CanceledExecutionException { }
}