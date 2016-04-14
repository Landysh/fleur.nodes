package io.landysh.inflor.java.knime.nodes.Singlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
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
 * This is the model implementation of FindSinglets.
 * Attempts to identify and compare pulse shape parameters in order to restrict subsequent analysis to un aggregated particles. 
 *
 * @author Aaron Hart
 */
public class FindSingletsNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected FindSingletsNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    
    //TODO No tests.
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	/**
    	 * Identifies parameters of interest and constructs a model of all parameter pairs.  
    	 * A 2nd scan of the data evaluates all data against the developed model. 
    	 */

    	// Construct a singlets model.  Hopefully this will move to the dialog with a subset of the data.     	
    	SingletsModel singletsFinder = new SingletsModel(inData[0].getDataTableSpec().getColumnNames());
    	String[] singletsParameters = singletsFinder.getSingletsParameters();
    	Hashtable<String, ArrayList<Double>> data = readDataColumns(singletsParameters, inData, exec);
    	singletsFinder.setData(data);
    	singletsFinder.generateModel();
    	
    	//Now we create the output data containers
    	DataTableSpec singletsSpec = createsingletsSpec(inData[0].getDataTableSpec());
    	BufferedDataContainer singletsDataTable = exec.createDataContainer(singletsSpec);
    	
    	//Iterate over the table again and direct the data into the correct port depending on the model predictions.

    	for (DataRow inRow: inData[0]){
    		HashMap <String, Double> row = new HashMap <String, Double>();
        	for (String s: singletsParameters){
        		data.put(s, new ArrayList<Double>());
        	}
    		for (String s: singletsParameters){
    			int index = inData[0].getSpec().findColumnIndex(s);
    			DataCell cell = inRow.getCell(index);
    			Double val = ((DoubleValue)cell).getDoubleValue();
    			row.put(s, val);
    		}
    		if(singletsFinder.scoreRow(row)){
    			singletsDataTable.addRowToTable(inRow);
    		} else {
    			System.out.print(inRow.getKey() + " Skipped" ); 
    		}	
    	}
    	//Close containers and return the tables.
    	singletsDataTable.close();	
        return new BufferedDataTable[]{singletsDataTable.getTable()};
    }
    //TODO No tests.
    private Hashtable<String, ArrayList<Double>> readDataColumns(String[] singletsParameters, BufferedDataTable[] inData,
			ExecutionContext exec) {
		/**
		 * Scans the data table and returns a hashtable of double arrays for the previously identified parameters of interest.
		 */
    	
    	Hashtable <String,ArrayList<Double>> data = new Hashtable <String,ArrayList<Double>>();
    	for (String s: singletsParameters){
    		data.put(s, new ArrayList<Double>());
    	}
    	for (DataRow inRow: inData[0]){
    		for (String s: singletsParameters){
    			int index = inData[0].getSpec().findColumnIndex(s);
    			DataCell cell = inRow.getCell(index);
    			Double val = ((DoubleValue)cell).getDoubleValue();
    			data.get(s).add(val);
			
    		}
    	}
    	return data;
    	
    	}

	/**
     * {@inheritDoc}
     */
    
    //TODO No tests.
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    
    //TODO No tests.
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	DataTableSpec singletsSpec = createsingletsSpec(inSpecs[0]);
        DataTableSpec[] outSpecs = {singletsSpec};
        return outSpecs;
    }
    
    //TODO No tests.
    private DataTableSpec createsingletsSpec(DataTableSpec dataTableSpec) {
		// do something more interesting?
		return dataTableSpec;
	}

	/**
     * {@inheritDoc}
     */
    
    //TODO No tests.
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
   
    //TODO No tests.
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    
    //TODO No tests.
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
   
    //TODO No tests.
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
   
    //TODO No tests.
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}