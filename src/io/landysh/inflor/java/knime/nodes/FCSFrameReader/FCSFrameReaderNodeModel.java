package io.landysh.inflor.java.knime.nodes.FCSFrameReader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;

import io.landysh.inflor.java.core.EventFrame;
import io.landysh.inflor.java.core.FCSFileReader;
import io.landysh.inflor.java.knime.nodes.FCSReader.FCSReaderNodeModel;
import io.landysh.inflor.java.knime.portTypes.fcs.FCSObjectSpec;
import io.landysh.inflor.java.knime.portTypes.fcs.FCSPortObject;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of FCSFrameReader.
 * This node reads an FCS file into an FCS Frame port
 *
 * @author Aaron Hart
 */
public class FCSFrameReaderNodeModel extends NodeModel {
    
	private static final NodeLogger logger = NodeLogger.getLogger(FCSReaderNodeModel.class);
	
	static final String CFGKEY_FileLocation = "File Location";

	/** initial default count value. */
	static final String DEFAULT_FileLocation = "";

	// example value: the models count variable filled from the dialog
	// and used in the models execution method. The default components of the
	// dialog work with "SettingsModels".
	private final SettingsModelString m_FileLocation = new SettingsModelString(CFGKEY_FileLocation,
			DEFAULT_FileLocation);

	static FCSFileReader FCS_READER;
    

    /**
     * Constructor for the node model.
     */
    protected FCSFrameReaderNodeModel() {
    
    	super(new PortType[0], new PortType[]{PortTypeRegistry.getInstance().getPortType(FCSPortObject.class)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

		logger.info("Starting Execution");
		// get table specs
		FCSFileReader FCSReader;
		Hashtable <String, String> keywords = null;
		double [][] data = null;
		try {
			FCSReader = new FCSFileReader(m_FileLocation.getStringValue());
			EventFrame frame = FCSReader.getEventFrame();
			keywords = FCSReader.getHeader();
			FCSObjectSpec[] tableSpecs = createPortSpec(frame);
			// Read header section
			////////////
			header = exec.create
			Enumeration<String> enumKey = keywords.keys();
			int i = 0;
			while (enumKey.hasMoreElements()) {
				String key = enumKey.nextElement();
				String val = keywords.get(key);
				RowKey rowKey = new RowKey("Row " + i);
				// the cells of the current row, the types of the cells must match
				// the column spec (see above)
				DataCell[] keywordCells = new DataCell[2];
				keywordCells[0] = new StringCell(key);
				keywordCells[1] = new StringCell(val);
				DataRow keywordRow = new DefaultRow(rowKey, keywordCells);
				header.addRowToTable(keywordRow);
				i++;
				if (key.equals("0") && val.equals("0"))
					keywords.remove(key);
			}
			header.close();

			// a quick breath before we move on.
			exec.checkCanceled();
			exec.setProgress(0.01, "Header read.");

			// Read data section
			data = exec.createDataContainer(tableSpecs[1]);
			FCSReader.initRowReader();
			for (int j = 0; j<frame.eventCount; j++) {
				RowKey rowKey = new RowKey("Row " + j);
				DataCell[] dataCells = new DataCell[frame.parameterCount+1];
				double[] FCSRow = FCSReader.readRow();
				for (int k=0; k<frame.parameterCount; k++) {
					dataCells[k] = new DoubleCell(FCSRow[k]);
				}
				dataCells[frame.parameterCount] = new DoubleCell(new Double(j));
				DataRow dataRow = new DefaultRow(rowKey, dataCells);
				data.addRowToTable(dataRow);
				if (j % 1000 == 0) {
					exec.checkCanceled();
					exec.setProgress(j / (double)frame.eventCount, j + " rows read.");
				}
			}
			// once we are done, we close the container and return its table
			data.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new CanceledExecutionException("Execution Failed.");
		}
		
		return new BufferedDataTable[] { header.getTable(), data.getTable() };
        return new BufferedDataTable[]{out};
    }

    private FCSObjectSpec[] createPortSpec(EventFrame frame) {
		// TODO Auto-generated method stub
		return null;
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
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
        
        m_count.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.
        
        m_count.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.

        m_count.validateSettings(settings);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

