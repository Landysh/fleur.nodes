package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.ColumnFilter;

public class ColumnStoreCellColumnFilter implements ColumnFilter {

	@Override
	public boolean includeColumn(DataColumnSpec colSpec) {
		if (colSpec.getType() == ColumnStoreCell.TYPE) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String allFilteredMsg() {
		return "No FCS compatible columns.";
	}

}
