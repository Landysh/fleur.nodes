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
package inflor.core.gates;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.proto.FCSFrameProto;
import inflor.core.proto.FCSFrameProto.Message.Subset.Type;
import inflor.core.utils.FCSUtilities;

public class RangeGate extends AbstractGate {

  private static final long serialVersionUID = -4829977491684130257L;
  ArrayList<RangeDimension> dimensions = new ArrayList<>();
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
      Optional<FCSDimension> xDimension = FCSUtilities.findCompatibleDimension(fcsFrame, xName);
      double[] xData = xDimension.get().getPreferredTransform().transform(xDimension.get().getData());
      Optional<FCSDimension> yDimension = FCSUtilities.findCompatibleDimension(fcsFrame, yName);
      double[] yData = yDimension.get().getPreferredTransform().transform(yDimension.get().getData());
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
      double[] data = FCSUtilities.findCompatibleDimension(fcsFrame, name).get().getData();
      BitSet dimesnionBits = dim.evaluate(data);
      result.and(dimesnionBits);
    }
    return result;
  }

  public List<String> getDimensionNames() {
    return dimensions
        .stream()
        .map(RangeDimension::getName)
        .collect(Collectors.toList());
  }

  @Override
  public Element toXMLElement() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (dimensions == null || dimensions.size() <= 1) {
      final String message = "A range gate must have at least 1 dimension";
      final IllegalArgumentException ise = new IllegalArgumentException(message);
      ise.printStackTrace();
      throw ise;
    }
  }

  @Override
  public String toString() {
    if (this.label == null) {
      return this.getID();
    } else {
      return label + String.join(File.pathSeparator, getDimensionNames());
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
    double[] xData = FCSUtilities.findCompatibleDimension(data, xName).get().getData();
    double[] yData = FCSUtilities.findCompatibleDimension(data, yName).get().getData();
    BitSet bits = new BitSet(data.getRowCount());
    for (int i = 0; i < xData.length; i++) {
      if (xMin < xData[i] && xData[i] < xMax && yMin < yData[i] && yData[i] < yMax) {
        bits.set(i);
      }
    }
  }
  @Override
  public String getLabel() {
    return this.label;
  }

  public double getMinValue(int i) {
    return dimensions.get(i).min;
  }
  
  public double getMaxValue(int i) {
    return dimensions.get(i).max;
  }

  @Override
  public Type getType() {
    return FCSFrameProto.Message.Subset.Type.RANGE;
  }

  @Override
  public String[] getDimensions() {
    String[] dimensionNames = new String[dimensions.size()];
    for (int i=0;i<dimensionNames.length;i++)
      dimensionNames[i] = dimensions.get(i).getName();
    return dimensionNames;
  }

  @Override
  public Double[] getDescriptors() {
    int size = dimensions.size()*2;
    Double[] descriptors = new Double[size];
    for (int i=0;i<dimensions.size();i++){
      descriptors[2*i] = dimensions.get(i).min;
      descriptors[2*i+1] = dimensions.get(i).max;
    }
    return descriptors;
  }
}
