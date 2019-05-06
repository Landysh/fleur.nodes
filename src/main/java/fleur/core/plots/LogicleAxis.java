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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.data.Range;

import fleur.core.transforms.LogicleTransform;

@SuppressWarnings("serial")
public class LogicleAxis extends NumberAxis {

  private LogicleTransform transform;

  protected final NumberFormat numberFormatterObj = NumberFormat.getInstance();

  LogicleAxis(String label, LogicleTransform transform) {
    super(label);
    this.transform = transform;
    initializeNumberFormat();
  }

  private void initializeNumberFormat() {
    ((DecimalFormat) this.numberFormatterObj).applyPattern("0E0");
  }

  public double transformValue(double value) {
    return transform.transform(value);
  }

  public double inverseValue(double value) {
    return transform.inverse(value);
  }

  /**
   * Rescales the axis to ensure that all data is visible.
   */
  @Override
  public void autoAdjustRange() {

    Plot plot = getPlot();
    if (plot == null) {
      return; // no plot, no data.
    }
    setRange(new Range(0, 262144), false, false);
  }
}
