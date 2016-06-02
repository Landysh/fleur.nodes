package io.landysh.inflor.java.knime.nodes.downsample;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.java.core.ColumnStore;
import io.landysh.inflor.java.core.FCSUtils;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;


/**
 * This is the model implementation of Downsample.
 * 
 *
 * @author Landysh Incorportated
 */
public class DownsampleNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(DownsampleNodeModel.class);
        
	//Downsample size
	static final String CFGKEY_Size = "size";
	static final int DEFAULT_Size = 5000;

	private final SettingsModelInteger m_Size = new SettingsModelInteger(
			CFGKEY_Size,
			DEFAULT_Size
			);
	
	
	/**
     * Constructor for the node model.
     */
    protected DownsampleNodeModel() {
    	super(new PortType[]{PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class)},
    			new PortType[]{PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class)});

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	ColumnStorePortObject inPort = (ColumnStorePortObject) inData[0];
    	ColumnStorePortSpec inSpec = (ColumnStorePortSpec) inPort.getSpec();
    	ColumnStore inColumnStore = inPort.getColumnStore();
    	int inSize = inColumnStore.getRowCount();
        int downSize = m_Size.getIntValue();
        ColumnStore outStore = new ColumnStore(inColumnStore.getKeywords(), inColumnStore.getColumnNames());
        if (downSize >= inSize){
        	outStore.setData(inColumnStore.getData());
        } else {
        	boolean[] mask = getShuffledMask(inSize,downSize);
    		for (String name:inColumnStore.getColumnNames()){
    			double[] maskedColumn = FCSUtils.getMaskColumn(mask, inColumnStore.getColumn(name));
    			outStore.addColumn(name, maskedColumn);
    		}
        }
        
        ColumnStorePortSpec outSpec = getSpec(inSpec);
		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
		FileStore filestore = fileStoreFactory.createFileStore("column.store");
		ColumnStorePortObject outPort = ColumnStorePortObject.createPortObject(outSpec, outStore, filestore);

		return new ColumnStorePortObject[]{outPort} ;
    }
    private boolean[] getShuffledMask(int inSize, int downSize) {
    	/**	Based on a knuth shuffle
    	 *  https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#The_modern_algorithm	
    	 */
    	//make an array of indices
    	int[] indices = new int[inSize];
    	for (int i=0;i<inSize;i++){
    		indices[i] = i;
    	}
    	//Init random number
    	Random rand = new Random((long) -1);
    	boolean[] mask = new boolean[inSize];
    	//The knuthy part
    	for (int i=0;i<downSize;i++){
            int pos = i + rand.nextInt(inSize - i);
            int temp = indices[pos];
            indices[pos] = indices[i];
            indices[i] = temp;
            mask[temp] = true;
    	}
    	return mask;
	}

	private ColumnStorePortSpec getSpec(ColumnStorePortSpec inSpec){
		ColumnStorePortSpec outSpec = new ColumnStorePortSpec(
    			inSpec.keywords, 
    			inSpec.columnNames, 
    			inSpec.getRowCount()
    			);
    	return outSpec;
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
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	ColumnStorePortSpec portSpec = (ColumnStorePortSpec) inSpecs[0];
    	ColumnStorePortSpec outSpec = new ColumnStorePortSpec(
    			portSpec.keywords, 
    			portSpec.columnNames, 
    			portSpec.getRowCount()
    			);
        return new ColumnStorePortSpec[]{outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        
        m_Size.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_Size.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        if (m_Size.getIntValue() >= 1){
            m_Size.validateSettings(settings);
        } else {
        	throw new InvalidSettingsException("Downsample size must be greater than 1");
        }
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

