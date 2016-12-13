package io.landysh.inflor.main.knime.dataTypes.FCSFrameCell;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFactory;

public class FCSFrameCellRenererFactory1 implements DataValueRendererFactory {

  private static final String DESCRIPTION = "FCS Frame Cell Summary";
  private static final String ID = "io.landysh.inflor.FCSFrameCellRenderer";

  public FCSFrameCellRenererFactory1() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public DataValueRenderer createRenderer(DataColumnSpec colSpec) {
    FCSFrameDataValueRenderer renderer = new FCSFrameDataValueRenderer("Foobar");
    return renderer;
  }

}
