package fleur.knime.nodes.fcs.read.set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.util.ColumnFilter;

public class StringCellColumnFilter implements ColumnFilter {

  @Override
  public boolean includeColumn(DataColumnSpec colSpec) {
    return colSpec.getType().equals(StringCell.TYPE);
  }

  @Override
  public String allFilteredMsg() {
    return "No string cells found.";
  }

}
