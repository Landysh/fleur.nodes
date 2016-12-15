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

import java.util.HashMap;

import org.jfree.data.xy.DefaultXYZDataset;

@SuppressWarnings("serial")
public class CategoryXYZDataSet extends DefaultXYZDataset {

  private HashMap<Integer, String> labelMap;

  public CategoryXYZDataSet() {
    super();
    labelMap = new HashMap<Integer, String>();
  }

  public synchronized void addCategoricalSeries(String name, double[] xValues, double[] zValues) {
    double[] yValues = new double[xValues.length];
    int yValue = this.getSeriesCount();
    for (int i = 0; i < yValues.length; i++) {
      yValues[i] = yValue;
    }
    labelMap.put(yValue, name);
    this.addSeries(name, new double[][] {xValues, yValues, zValues});
  }

  public HashMap<Integer, String> getLabelMap() {
    return this.labelMap;
  }
}
