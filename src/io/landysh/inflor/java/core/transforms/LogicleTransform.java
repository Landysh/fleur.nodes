package io.landysh.inflor.java.core.transforms;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.stanford.facs.logicle.FastLogicle;

@SuppressWarnings("serial")
public class LogicleTransform extends AbstractTransform implements Serializable {

  private static final double LOGICLE_W_PERCENTILE = 1;
  private static final double dt = 262144;
  private static final double dw = 0.5;
  private static final double dm = 4.5;
  private static final double da = 0;
  transient FastLogicle logicle;
  private double t;
  private double w;
  private double m;
  private double a;

  public LogicleTransform() {
    this(dt, dw, dm, da);
  }

  public LogicleTransform(double t2, double w2, double m2, double a2) {
    this.t = t2;
    this.w = w2;
    this.m = m2;
    this.a = a2;
    this.logicle = new FastLogicle(t, w, m, a);

  }

  @Override
  public double[] transform(double[] rawData) {
    double[] newData = new double[rawData.length];
    for (int i = 0; i < rawData.length; i++) {
      newData[i] = transform(rawData[i]);
    }
    return newData;
  }

  public double[] inverse(double[] transformedData) {
    double[] newData = new double[transformedData.length];
    for (int i = 0; i < transformedData.length; i++) {
      newData[i] = logicle.inverse(transformedData[i]);
    }
    return newData;
  }

  public void optimizeW(double[] data) {
    /**
     * Based on the percentile method suggested by Parks/Moore.
     */
    double lowerBound = new Percentile().evaluate(data, LOGICLE_W_PERCENTILE);
    double newWidth = (m - Math.log10(t / Math.abs(lowerBound))) / 2;
    if (newWidth < 0) {
      newWidth = this.w;// reasonable?
    }
    this.w = newWidth;
    this.logicle = new FastLogicle(logicle.T, newWidth, logicle.M, logicle.A);
  }

  @Override
  public double transform(double value) {
    if (value < getMinRawValue()) {
      value = getMinRawValue();
    } else if (value >= getMaxRawValue()) {
      value = getMaxRawValue();
    } else {
      // noop
    }

    if (value == logicle.T) {
      return 1;
    }
    return logicle.scale(value);
  }

  @Override
  public double inverse(double value) {
    if (value == 1) {
      return logicle.T;// TODO: Is that right?
    } else {
      return logicle.inverse(value);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.logicle = new FastLogicle(t, w, m, a);
  }

  @Override
  public double getMinTranformedValue() {
    return 0;
  }

  @Override
  public double getMaxTransformedValue() {
    return 1;
  }

  @Override
  public double getMinRawValue() {
    return inverse(0);
  }

  @Override
  public double getMaxRawValue() {
    return t;
  }

  public double[] getAxisValues() {
    return logicle.axisLabels();
  }

  public double getT() {
    return this.t;
  }

  public double getW() {
    return this.w;
  }

  public double getM() {
    return this.m;
  }

  public double getA() {
    return this.a;
  }
}
