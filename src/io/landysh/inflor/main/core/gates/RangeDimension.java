package io.landysh.inflor.main.core.gates;

import java.io.Serializable;
import java.util.BitSet;

@SuppressWarnings("serial")
public class RangeDimension implements Serializable{

  public double min = Double.MIN_VALUE;
  public double max = Double.MAX_VALUE;
  private String name;

  public RangeDimension(String name, double min, double max) {
    this.name = name;
    this.min = min;
    this.max = max;
  }
  
//  public RangeDimension() { }

  public BitSet evaluate(double[] data) {
    BitSet result = new BitSet(data.length);
    for (int i = 0; i < data.length; i++) {
      if (min <= data[i] && data[i] < max) {
        result.set(i);
      }
    }
    return result;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
