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
package io.landysh.inflor.main.core.plots;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

@SuppressWarnings("serial")
public class CategoricalNumberAxis extends NumberAxis {

  private HashMap<Integer, String> lableMap;

  public CategoricalNumberAxis(String name, HashMap<Integer, String> lableMap) {
    super(name);
    this.lableMap = lableMap;

    Integer[] yValues = lableMap.keySet().toArray(new Integer[lableMap.size()]);
    double yMin = Double.MAX_VALUE;
    double yMax = Double.MIN_VALUE;

    for (Integer d : yValues) {
      if (d < yMin) {
        yMin = d;
      }
      if (d > yMax) {
        yMax = d;
      }
    }
    this.setRange(new Range(yMin - 0.5, yMax + 0.5));
    NumberFormat formatter = new CategoryNumberFormat(lableMap);
    this.setNumberFormatOverride(formatter);
    this.setTickMarkOutsideLength(2);
  }

  @Override
  public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea,
      RectangleEdge edge) {
    List<NumberTick> ticks = new ArrayList<NumberTick>();
    for (Integer i : lableMap.keySet()) {
      ticks.add(new NumberTick(i, lableMap.get(i), TextAnchor.CENTER_RIGHT, TextAnchor.CENTER, 0));
    }
    return ticks;
  }
}
