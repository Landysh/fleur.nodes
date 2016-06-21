package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;

import io.landysh.inflor.java.core.ColumnStore;


public class ColumnStoreCell extends FileStoreCell {

	public static final class ColumnStoreCellSerializer implements DataCellSerializer<ColumnStoreCell> {

		@Override
		public void serialize(ColumnStoreCell cell, DataCellDataOutput output) throws IOException {
			byte[] bytes = cell.getColumnStore().save();
			output.writeInt(bytes.length);
			output.write(bytes);
		}	

		@Override
		public ColumnStoreCell deserialize(DataCellDataInput input) throws IOException {
			try{
				byte[] bytes = new byte[input.readInt()];
				input.readFully(bytes);
		        ColumnStore cStore;
				cStore = ColumnStore.load(bytes);
				ColumnStoreCell newCell = new ColumnStoreCell(cStore);
		        return newCell;
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Error during deserialization");
			}	
		}
    }
	/**
	 *  A cell type matching the functionality of the ColumnStorePortObject.
	 */
	private ColumnStore m_data;
	private static final long serialVersionUID = 1L;
	public static final DataType TYPE = DataType.getType(ColumnStoreCell.class, ColumnStoreCell.TYPE);


	public ColumnStoreCell(FileStore fs, ColumnStore cStore) {
			super(fs);
			this.m_data = cStore;
		}

	ColumnStoreCell(ColumnStore cStore) {
		//Use with deserializer.
		this.m_data = cStore;
	}

	@Override
	public String toString(){
		return "Foo";
	}

	public ColumnStore getColumnStore() {
		return this.m_data;
	}
}