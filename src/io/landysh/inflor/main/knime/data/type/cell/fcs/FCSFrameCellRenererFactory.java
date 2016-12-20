/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package io.landysh.inflor.main.knime.data.type.cell.fcs;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFactory;

public class FCSFrameCellRenererFactory implements DataValueRendererFactory {

  private static final String DESCRIPTION = "FCS Frame Cell Summary";
  private static final String ID = "io.landysh.inflor.FCSFrameCellRenderer";

  public FCSFrameCellRenererFactory() {
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
