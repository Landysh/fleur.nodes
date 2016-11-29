package io.landysh.inflor.main.core.transforms;

import com.google.common.primitives.Doubles;

public class LogrithmicTransform extends AbstractTransform {

  /**
   * 
   */
  private static final long serialVersionUID = 5678244382085424064L;

  private double min;
  private double max;


  public LogrithmicTransform(double min, double max, String priorID) {
    super(priorID);
    this.min = min;
    this.max = max;
  }
  
  public LogrithmicTransform(double min, double max) {
    this(min, max, null);
  }

  @Override
  public double[] transform(double[] rawData) {
    double[] transformedData = new double[rawData.length];
    for (int i = 0; i < rawData.length; i++) {
      transformedData[i] = transform(rawData[i]);
    }
    return transformedData;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }

  @Override
  public double transform(double value) {
    if (value < min) {
      value = min;
    }
    return Math.log10(value);
  }

  @Override
  public double inverse(double value) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getMinTranformedValue() {
    return Math.log(min);
  }

  @Override
  public double getMaxTransformedValue() {
    return Math.log(max);
  }

  @Override
  public double getMinRawValue() {
    return min;
  }

  @Override
  public double getMaxRawValue() {
    return max;
  }

  public void optimize(double[] data) {
    min = Doubles.min(data);
    max = Doubles.max(data);
  }
}
