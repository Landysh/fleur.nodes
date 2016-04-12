package io.landysh.inflor.nodes.knime.FindSinglets;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.SingletsModel;

/**
 * This is the model implementation of RemoveDoublets.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected RemoveDoubletsNodeModel() {
        super(1, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	DataTableSpec singletsSpec = createsingletsSpec(inData[0].getDataTableSpec());
    	BufferedDataContainer singletsDataTable = exec.createDataContainer(singletsSpec);

    	DataTableSpec doubletsSpec = createsingletsSpec(inData[0].getDataTableSpec());
    	BufferedDataContainer doubletsDataTable = exec.createDataContainer(doubletsSpec);
    	    	
    	SingletsModel model = new SingletsModel(singletsSpec.getColumnNames());
    	
    	singletsDataTable.close();	
    	doubletsDataTable.close();
        return new BufferedDataTable[]{singletsDataTable.getTable(),doubletsDataTable.getTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	DataTableSpec singletsSpec = createsingletsSpec(inSpecs[0]);
    	DataTableSpec doubletsSpec = createsingletsSpec(inSpecs[0]);
        DataTableSpec[] outSpecs = {singletsSpec, doubletsSpec};
        return outSpecs;
    }

    private DataTableSpec createsingletsSpec(DataTableSpec dataTableSpec) {
		// do something more interesting?
		return dataTableSpec;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

