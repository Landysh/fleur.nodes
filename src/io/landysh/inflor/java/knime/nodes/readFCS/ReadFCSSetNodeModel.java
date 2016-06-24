package io.landysh.inflor.java.knime.nodes.readFCS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import io.landysh.inflor.java.core.ColumnStore;
import io.landysh.inflor.java.core.FCSFileReader;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;


/**
 * This is the model implementation of ReadFCSSet.
 * 
 *
 * @author Landysh Co.
 */
public class ReadFCSSetNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(ReadFCSSetNodeModel.class);
    
    //Folder containing FCS Files.
	static final String CFGKEY_PATH = "Path";
    static final String DEFAULT_PATH = null;
    private final SettingsModelString m_path 
    	= new SettingsModelString(CFGKEY_PATH, DEFAULT_PATH);
    
    //List of keywords to include in the output table.
//  private static final String CFGKEY_SELECTED_KEYWORDS = "Keywords";
    private final String[] DEFAULT_KEYWORDS = new String[] {"$FIL"};
//  private final SettingsModelStringArray m_selectedKeywords 
//  = new SettingsModelStringArray(CFGKEY_SELECTED_KEYWORDS, DEFAULT_KEYWORDS);
    
    //List of files to read.
//	private static final String CFGKEY_SELECTED_FILES = "FILES";
//  private String[] files = new String[] {};
//  private final SettingsModelStringArray m_selectedFiles 
//  	= new SettingsModelStringArray(CFGKEY_SELECTED_FILES, DEFAULT_FILES);
    
    //Should we compensate?
	static final String CFGKEY_COMPENSATE = "Compensate";
    static final boolean DEFAULT_COMPENSATE = false;
    private final SettingsModelBoolean m_compensate = new SettingsModelBoolean(
    													  CFGKEY_COMPENSATE, 
    													  DEFAULT_COMPENSATE);

    /**
     * Constructor for the node model.
     */
    protected ReadFCSSetNodeModel() {
            super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Beginning Execution.");

        // Create the output spec and data container.
        DataTableSpec[] outSpecs = createSpecs();
        BufferedDataContainer container = exec.createDataContainer(outSpecs[0]);
        int columnCount = 1 + DEFAULT_KEYWORDS.length;//m_selectedKeywords.getStringArrayValue().length;
        String[] filePaths = getFilePaths(m_path.getStringValue());
        int rowCount = filePaths.length;
        
		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
		
        //Read all the files.
        for (int i = 0; i < rowCount; i++) {
            RowKey key = new RowKey("Row " + i);
            DataCell[] cells = new DataCell[columnCount];
        	String pathToFile = filePaths[i];
			FCSFileReader FCSReader = new FCSFileReader(pathToFile, m_compensate.getBooleanValue());
    		FCSReader.readColumnEventData();
    		ColumnStore columnStore = FCSReader.getColumnStore();
    		String fsName = i+ "ColumnStore.fs";
    		FileStore fileStore = fileStoreFactory.createFileStore(fsName);

    		ColumnStoreCell fileCell= new ColumnStoreCell(fileStore, columnStore);
            cells[0] = fileCell;
            for (int j=0;j<columnCount-1;j++){
            	String keyword = DEFAULT_KEYWORDS[j];//m_selectedKeywords.getStringArrayValue()[j];
            	String value = fileCell.getColumnStore().getKeywordValue(keyword);
            	cells[j+1] = new StringCell(value);            	
            }
            DataRow row = new DefaultRow(key, cells);
            container.addRowToTable(row);
            
            // check if the execution monitor was canceled
            exec.checkCanceled();
            exec.setProgress(i / (double)rowCount, "Reading file " + (i+1));
        }
        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }

	private DataTableSpec[] createSpecs() {
		String[] keywordNames = DEFAULT_KEYWORDS;//m_selectedKeywords.getStringArrayValue();
		DataColumnSpec[] colSpecs = new DataColumnSpec[keywordNames.length + 1 ];
		for (int i=0;i<colSpecs.length;i++){
			if (i==0){
				colSpecs[i] = new DataColumnSpecCreator("FCS Frame", ColumnStoreCell.TYPE).createSpec();
			} else {
				colSpecs[i] = new DataColumnSpecCreator(keywordNames[i-1], StringCell.TYPE).createSpec();
			}
		}
		DataTableSpec tableSpec = new DataTableSpec(colSpecs);
		return new DataTableSpec[]{tableSpec};
	}

	private String[] getFilePaths(String stringValue) {
		File folder = new File(m_path.getStringValue());
		File[] files = folder.listFiles();
		ArrayList<String> validFiles = new ArrayList<String>();
		for (int i=0; i<files.length; i++) {
			String filePath = files[i].getAbsolutePath();
			if(FCSFileReader.isValidFCS(filePath)==true){
				validFiles.add(filePath);
			} else if (files[i].isDirectory()) {
				System.out.println("Directory " + files[i].getName());
			}
		}
		return (String[]) validFiles.toArray(new String[validFiles.size()]);
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
		DataTableSpec[] specs = createSpecs();
        return specs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_path.saveSettingsTo(settings);
       // m_selectedKeywords.saveSettingsTo(settings);
        m_compensate.saveSettingsTo(settings);
        //m_selectedFiles.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
        m_path.loadSettingsFrom(settings);
        //m_selectedKeywords.loadSettingsFrom(settings);
        m_compensate.loadSettingsFrom(settings);
        //m_selectedFiles.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_path.validateSettings(settings);
        //m_selectedKeywords.validateSettings(settings);
        m_compensate.validateSettings(settings);
        //m_selectedFiles.validateSettings(settings);
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