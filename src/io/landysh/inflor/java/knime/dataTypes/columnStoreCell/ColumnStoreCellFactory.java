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

	private final FileStoreFactory m_fileStoreFactory;

	public ColumnStoreCellFactory() {
		/**
		 * Use with views
		 */
		m_fileStoreFactory = FileStoreFactory.createNotInWorkflowFileStoreFactory();
	}

	public ColumnStoreCellFactory(ExecutionContext exec) {
		/**
		 * Use during node execution.
		 */
		m_fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
	}

	@Override
	public DataCell createCell(InputStream input) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileUtil.copy(input, output);
		output.close();
		final byte[] buffer = output.toByteArray();
		// Create the file store.
		final FileStore fs = m_fileStoreFactory.createFileStore("column.store");
		final ColumnStoreCell cell = new ColumnStoreContent(buffer).toColumnStoreCell(fs);
		return cell;
	}

	@Override
	public DataType getDataType() {
		return null;
	}

}
