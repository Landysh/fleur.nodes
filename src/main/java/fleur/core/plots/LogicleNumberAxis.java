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
package fleur.core.plots;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import fleur.core.transforms.LogicleTransform;

@SuppressWarnings("serial")
public class LogicleNumberAxis extends NumberAxis {

  private LogicleTransform logicle;

  public LogicleNumberAxis(String name, LogicleTransform transform) {
    super(name);
    this.logicle = transform;
    this.setRange(new Range(transform.getMinTranformedValue(), transform.getMaxTransformedValue()));
    NumberFormat formatter = new LogicleNumberFormat(transform);
    this.setNumberFormatOverride(formatter);
    this.setTickMarkOutsideLength(2);
  }

  @Override
  public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea,
      RectangleEdge edge) {
    List<NumberTick> ticks = new ArrayList<>();
    double[] suggestedTicks = logicle.getAxisValues();
    if (suggestedTicks[0] >= 0) {
      double tick1Value = logicle.getMinTranformedValue();
      double tick2Value = logicle.transform(Math.abs(logicle.getMinRawValue()));

      String label1 = this.getNumberFormatOverride().format(tick1Value);
      String label2 = this.getNumberFormatOverride().format(tick2Value);

      ticks
          .add(new NumberTick(tick1Value, label1, TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0));
      ticks
          .add(new NumberTick(tick2Value, label2, TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0));
    }

    for (double value : suggestedTicks) {
      double td = logicle.transform(value);
      double tickDangerRatio = 0;
      if (ticks.size() >= 2) {
        double firstPositiveTick = logicle.inverse(ticks.get(1).getValue());
        tickDangerRatio = value / firstPositiveTick;
      }
      if (tickDangerRatio < 0.5 || tickDangerRatio > 2) {
        String label = this.getNumberFormatOverride().format(td);
        NumberTick tick =
            new NumberTick(td, label, TextAnchor.TOP_CENTER, TextAnchor.TOP_CENTER, 0);
        ticks.add(tick);
      }
    }
    return ticks;
  }
}
