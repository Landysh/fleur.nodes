package io.landysh.inflor.java.core.gates;

import java.util.BitSet;

public class RangeDimension extends AbstractGMLDimension {

  public double min = Double.MIN_VALUE;
  public double max = Double.MAX_VALUE;

  public RangeDimension(String name, double min, double max) {
    super(name);
    this.min = min;
    this.max = max;
  }

  public BitSet evaluate(double[] data) {
    BitSet result = new BitSet(data.length);
    for (int i = 0; i < data.length; i++) {
      if (min <= data[i] && data[i] < max) {
        result.set(i);
      }
    }
    return result;
  }
}
