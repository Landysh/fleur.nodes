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
package io.landysh.inflor.main.core.gates;

import java.util.ArrayList;
import java.util.BitSet;

import org.w3c.dom.Element;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.utils.FCSUtilities;

public class RectangleGate extends AbstractGate {

  /**
   * -8160323086009163230L;
   */
  private static final long serialVersionUID = -8160323086009163230L;
  ArrayList<RangeDimension> dimensions = new ArrayList<RangeDimension>();
  private String label;
  private String xName;
  private double xMin;
  private double xMax;
  private String yName;
  private double yMin;
  private double yMax;

  public RectangleGate(String label, String xName, double xMin, double xMax, String yName,
      double yMin, double yMax, String priorUUID) {
    super(priorUUID);
    this.label = label;
    this.xName = xName;
    this.xMin = xMin;
    this.xMax = xMax;
    this.yName = yName;
    this.yMin = yMin;
    this.yMax = yMax;

  }

  public RectangleGate(String label, String xName, double xMin, double xMax, String yName,
      double yMin, double yMax) {
    this(label, xName, xMin, xMax, yName, yMin, yMax, null);
  }

  @Override
  public Element toXMLElement() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void validate() throws IllegalStateException {
    if (dimensions == null || dimensions.size() <= 1) {
      final String message = "A range gate must have at least 1 dimension";
      final IllegalStateException ise = new IllegalStateException(message);
      ise.printStackTrace();
      throw ise;
    }
  }

  @Override
  public String toString() {
    if (this.label == null) {
      return getID();
    } else {
      return label;
    }
  }

  @Override
  public String getDomainAxisName() {
    return yName;
  }

  @Override
  public String getRangeAxisName() {
    return xName;
  }

  public BitSet evaluate(FCSFrame data) {
    double[] xData = FCSUtilities.findCompatibleDimension(data, xName).getData();
    double[] yData = FCSUtilities.findCompatibleDimension(data, yName).getData();
    BitSet bits = new BitSet(data.getRowCount());
    for (int i = 0; i < xData.length; i++) {
      if (this.xMin < xData[i] && xData[i] < this.xMax && this.yMin < yData[i]
          && yData[i] < this.yMax) {
        bits.set(i);
      }
    }
    return bits;
  }

  @Override
  public String getLabel() {
    return this.label;
  }
}
