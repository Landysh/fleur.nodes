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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import fleur.core.transforms.LogicleTransform;

@SuppressWarnings("serial")
public class LogicleNumberFormat extends NumberFormat {

  private LogicleTransform transform;

  public LogicleNumberFormat(LogicleTransform transform) {
    this.transform = transform;
  }

  @Override
  public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2) {
    double val = transform.inverse(arg0);
    int iVal = (int) Math.round(val);


    String tickLabel;
    if (Math.abs(iVal) <= 990) {
      tickLabel = Integer.toString(iVal);
    } else {
      tickLabel = LogicleTickFormatter.findLogTick(iVal);
    }
    return new StringBuffer(tickLabel);
  }

  @Override
  public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2) {
    // TODO: needed?
    return null;
  }

  @Override
  public Number parse(String arg0, ParsePosition arg1) {
    double d = Double.parseDouble(arg0);
    return transform.transform(d);
  }
}
