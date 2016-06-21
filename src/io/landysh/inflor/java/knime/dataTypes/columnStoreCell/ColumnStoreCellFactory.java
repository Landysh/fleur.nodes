package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import org.knime.core.data.DataCell;

import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;


@SuppressWarnings("serial")
public class ColumnStoreCellFactory extends DataCell {

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean equalsDataCell(DataCell dc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static DataCell createCell(ColumnStorePortObject columnStorePortObject) {
		// TODO Auto-generated method stub
		return null;
	}

}	
