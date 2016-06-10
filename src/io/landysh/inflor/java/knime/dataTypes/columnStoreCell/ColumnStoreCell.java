package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import org.knime.core.data.DataType;
import org.knime.core.data.model.PortObjectCell;
import org.knime.core.node.port.PortObject;


public class ColumnStoreCell extends PortObjectCell {


	public ColumnStoreCell(PortObject content) {
		super(content);
		// TODO Auto-generated constructor stub
	}
	/**
	 *  A cell type matching the functionality of the ColumnStorePortObject.
	 */
	private static final long serialVersionUID = 1L;
		public static final DataType TYPE = DataType.getType(ColumnStoreCell.class, ColumnStoreCell.TYPE);

	@Override
	public String toString(){
		return "Foo";
	}

}
