package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import org.knime.core.data.DataCell;

import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;

public class ColumnStoreCellFactory {

	public static DataCell createCell(ColumnStorePortObject portObject) {
		ColumnStoreCell cell = new ColumnStoreCell(portObject);
		return cell;
	}

}	
