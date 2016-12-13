package io.landysh.inflor.main.knime.dataTypes.FCSFrameCell;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.ColumnFilter;

public class FCSFrameCellColumnFilter implements ColumnFilter {

  @Override
  public String allFilteredMsg() {
    return "No FCS compatible columns.";
  }

  @Override
  public boolean includeColumn(DataColumnSpec colSpec) {
    if (colSpec.getType() == FCSFrameFileStoreDataCell.TYPE) {
      return true;
    } else {
      return false;
    }
  }

}
