package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSVector;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.nodes.viabilityFilter.ViabilityModel;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of CreateGates.
 * 
 *
 * @author 
 */
public class CreateGatesNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(CreateGatesNodeModel.class);
        
    GatingModelNodeSettings m_settings;
    

    /**
     * Constructor for the node model.
     */
    protected CreateGatesNodeModel() {
        super(1, 1);
        
        m_settings = new GatingModelNodeSettings();
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Executing: Create Gates");
		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

		// Create the output spec and data container.
		DataTableSpec outSpec = createSpecs(inData[0].getSpec())[0];
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		String columnName = m_settings.getSelectedColumn();
		int index = outSpec.findColumnIndex(columnName);

		int i = 0;
		for (DataRow inRow : inData[0]) {
			DataCell[] outCells = new DataCell[inRow.getNumCells()];
			ColumnStore inStore = ((ColumnStoreCell) inRow.getCell(index)).getColumnStore();

			// now create the output row
			ColumnStore outStore = applyGates(inStore);
			String fsName = i + "ColumnStore.fs";
			FileStore fileStore = fileStoreFactory.createFileStore(fsName);
			ColumnStoreCell fileCell = new ColumnStoreCell(fileStore, outStore);

			for (int j = 0; j < outCells.length; j++) {
				if (j == index) {
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
		return new BufferedDataTable[] { container.getTable() };
    }

    private ColumnStore applyGates(ColumnStore inStore) {
		ColumnStore outStore = new ColumnStore(inStore.getKeywords(), inStore.getColumnNames());
		//TODO
		return outStore;
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	DataTableSpec[] outSpecs = createSpecs(inSpecs[0]);

        return outSpecs;
    }

    private DataTableSpec[] createSpecs(DataTableSpec inSpec) {
    	DataTableSpecCreator creator = new DataTableSpecCreator(inSpec);
    	DataTableSpec outSpec = creator.createSpec();
    	return new DataTableSpec[] {outSpec};
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
            CanceledExecutionException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}
//EOF