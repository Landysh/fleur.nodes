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
package inflor.core.plots;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class CategoryNumberFormat extends NumberFormat {

  private Map<Integer, String> labelMap;

  public CategoryNumberFormat(Map<Integer, String> labelMap) {
    this.labelMap = labelMap;
  }

  @Override
  public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2) {
    return new StringBuffer(labelMap.get((int) arg0));
  }

  @Override
  public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2) {
    // TODO I still dont understand this but it seems to work?
    return null;
  }

  @Override
  public Number parse(String arg0, ParsePosition arg1) {
    for (Entry<Integer, String> e : labelMap.entrySet()) {
      if (e.getValue() == arg0) {
        return e.getKey();
      }
    }
    return null;
  }
}