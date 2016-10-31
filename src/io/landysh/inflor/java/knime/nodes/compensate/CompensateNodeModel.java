package io.landysh.inflor.java.knime.nodes.compensate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gatingML.compensation.SpilloverCompensator;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;

/**
 * This is the model implementation of Compensate.
 * Will extract a compensation matrix from am FCS file and apply it to a group of files
 *
 * @author Aaron Hart
 */
public class CompensateNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(CompensateNodeModel.class);


	private CompensateNodeSettings m_settings;
    

    /**
     * Constructor for the node model.
     */
    protected CompensateNodeModel() {
    	super(1, 1);
    	this.m_settings = new CompensateNodeSettings();
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		logger.info("Beginning Execution.");
		final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

		// Create the output spec and data container.
		final DataTableSpec outSpec = createSpec(inData[0].getDataTableSpec());
		final BufferedDataContainer container = exec.createDataContainer(outSpec);
		final String columnName = m_settings.getSelectedColumn();
		final int index = outSpec.findColumnIndex(columnName);
		final HashMap <String, String> referenceHeader = m_settings.getReferenceHeader();
		SpilloverCompensator compr = new SpilloverCompensator(referenceHeader);
				
		int i = 0;
		for (final DataRow inRow : inData[0]) {
			
			final DataCell[] outCells = new DataCell[inRow.getNumCells()];
			final FCSFrame columnStore = ((ColumnStoreCell) inRow.getCell(index)).getFCSFrame();
			
			
			// now create the output row
			final FCSFrame outStore = compr.compensateFCSFrame(columnStore);
			final String fsName = i + "ColumnStore.fs";
			final FileStore fileStore = fileStoreFactory.createFileStore(fsName);
			final ColumnStoreCell fileCell = new ColumnStoreCell(fileStore, outStore);

			for (int j = 0; j < outCells.length; j++) {
				if (j == index) {
					outCells[j] = fileCell;
				} else {
					outCells[j] = inRow.getCell(j);
				}
			}
			final DataRow outRow = new DefaultRow("Row " + i, outCells);
			container.addRowToTable(outRow);
			i++;
		}
		container.close();
		return new BufferedDataTable[] { container.getTable() };
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		final DataTableSpec spec = createSpec(inSpecs[0]);
		return new DataTableSpec[] { spec };
	}

	private DataTableSpec createSpec(DataTableSpec dataTableSpec) {
		return dataTableSpec;}
    
	/**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings){
    	m_settings.save(settings);
    	}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {m_settings.load(settings);}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {m_settings.validate(settings);}
    
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
