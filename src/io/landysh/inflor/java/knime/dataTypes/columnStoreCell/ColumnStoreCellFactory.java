package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory.FromInputStream;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.FileUtil;

public class ColumnStoreCellFactory implements FromInputStream {
	
    /**
     * The data type for cells created using this factory.
     */
    public static final DataType TYPE = ColumnStoreContent.TYPE;
	
    private FileStoreFactory m_fileStoreFactory;
	
    public ColumnStoreCellFactory(ExecutionContext exec){
    	/**
    	 * Use during node execution.
    	 */
    	this.m_fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    }
    
    public ColumnStoreCellFactory(){
    	/**
    	 * Use with views
    	 */
    	this.m_fileStoreFactory = FileStoreFactory.createNotInWorkflowFileStoreFactory();
    }
    
    
	@Override
	public DataType getDataType() {
		return null;
	}

	@Override
	public DataCell createCell(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtil.copy(input, output);
        output.close();
        byte[] buffer = output.toByteArray();
		//Create the file store.
		FileStore fs = m_fileStoreFactory.createFileStore("column.store");
        ColumnStoreCell cell = new ColumnStoreContent(buffer).toColumnStoreCell(fs);
        return cell;
	}
	
}	
