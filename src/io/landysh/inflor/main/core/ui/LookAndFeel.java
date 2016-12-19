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
package io.landysh.inflor.main.core.ui;

import java.awt.BasicStroke;
import java.awt.Color;

public final class LookAndFeel {
 
  public static final BasicStroke DEFAULT_STROKE = new BasicStroke();
  public static final BasicStroke SELECTED_STROKE = new BasicStroke(2);
  public static final Color DEFAULT_GATE_COLOR = Color.BLACK;
  public static final Color SELECTED_GATE_COLOR = Color.MAGENTA;
  
  private LookAndFeel(){}
 
}
