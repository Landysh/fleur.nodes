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

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.utils.FCSUtilities;

public class RangeGate extends AbstractGate {

  private static final long serialVersionUID = -4829977491684130257L;
  ArrayList<RangeDimension> dimensions = new ArrayList<RangeDimension>();
  private String label;

  public RangeGate(String label, String[] names, double[] min, double[] max, String priorUUID) {
    super(priorUUID);
    this.label = label;
    if (names.length < 1) {
      throw new IllegalArgumentException(
          "CODING ERROR: Range gate array parameters must be of the same length and >=1");
    }
    for (int i = 0; i < names.length; i++) {
      dimensions.add(new RangeDimension(names[i], min[i], max[i]));
    }
  }

  public RangeGate(String label, String[] names, double[] min, double[] max) {
    this(label, names, min, max, null);
  }

  @Override
  public BitSet evaluate(FCSFrame fcsFrame) {
    // TODO performance optimization?
    if (dimensions.size() == 2) {
      String xName = dimensions.get(0).getName();
      double xMin = dimensions.get(0).min;
      double xMax = dimensions.get(0).max;
      String yName = dimensions.get(1).getName();
      double yMin = dimensions.get(1).min;
      double yMax = dimensions.get(1).max;
      FCSDimension xDimension = FCSUtilities.findCompatibleDimension(fcsFrame, xName);
      double[] xData = xDimension.getPreferredTransform().transform(xDimension.getData());
      FCSDimension yDimension = FCSUtilities.findCompatibleDimension(fcsFrame, yName);
      double[] yData = yDimension.getPreferredTransform().transform(yDimension.getData());
      BitSet bits = new BitSet(fcsFrame.getRowCount());
      for (int i = 0; i < xData.length; i++) {
        if (xMin < xData[i] && xData[i] < xMax && yMin < yData[i] && yData[i] < yMax) {
          bits.set(i);
        }
      }
      return bits;
    }

    int rowCount = fcsFrame.getRowCount();
    final BitSet result = new BitSet(rowCount);
    result.set(0, result.size() - 1);

    for (RangeDimension dim : dimensions) {
      String name = dim.getName();
      double[] data = FCSUtilities.findCompatibleDimension(fcsFrame, name).getData();
      BitSet dimesnionBits = dim.evaluate(data);
      result.and(dimesnionBits);
    }
    return result;
  }

  public String[] getDimensionNames() {
    String[] names = (String[]) dimensions.stream().map(dim -> dim.getName()).toArray();
    return names;
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
      return this.getID();
    } else {
      return label + this.getID();
    }
  }

  @Override
  public String getDomainAxisName() {
    return dimensions.get(0).getName();
  }

  @Override
  public String getRangeAxisName() {
    return dimensions.get(1).getName();
  }

  public void evaluateSimpleRect(FCSFrame data) {
    String xName = dimensions.get(0).getName();
    double xMin = dimensions.get(0).min;
    double xMax = dimensions.get(0).max;
    String yName = dimensions.get(1).getName();
    double yMin = dimensions.get(1).min;
    double yMax = dimensions.get(1).max;
    double[] xData = FCSUtilities.findCompatibleDimension(data, xName).getData();
    double[] yData = FCSUtilities.findCompatibleDimension(data, yName).getData();
    BitSet bits = new BitSet(data.getRowCount());
    for (int i = 0; i < xData.length; i++) {
      if (xMin < xData[i] && xData[i] < xMax && yMin < yData[i] && yData[i] < yMax) {
        bits.set(i);
      }
    }
  }

  public String getLabel() {
    return this.label;
  }

  public double getMinValue(int i) {
    return dimensions.get(i).min;
  }
  
  public double getMaxValue(int i) {
    return dimensions.get(i).max;
  }
}
