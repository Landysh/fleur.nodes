package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

import io.landysh.inflor.java.core.ColumnStore;

public class ColumnStoreContent {
	
	public static final class CellSerializer implements DataCellSerializer<ColumnStoreCell>{
	
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
		@Override
		public void serialize(ColumnStoreCell cell, DataCellDataOutput output) throws IOException {
			byte[] bytes = cell.getColumnStore().save();
			output.writeInt(bytes.length);
			output.write(bytes);
		}	
	}
}
