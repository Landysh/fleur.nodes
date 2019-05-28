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
package fleur.core.transforms;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import edu.stanford.facs.logicle.FastLogicle;

public class LogicleTransform extends AbstractTransform implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private static final double LOGICLE_T_PERCENTILE = 99.9;
  private static final double LOGICLE_W_PERCENTILE = 5;
  private static final double DEFAULT_W = 0.5;
  private static final double DEFAULT_M = 4.5;
  private static final double DEFAULT_A = 0.5;
  
  transient FastLogicle logicle;
  
  public LogicleTransform(double t2, double w2, double m2, double a2, String priorID) {
    super(priorID);
    try {
      this.logicle = new FastLogicle(t2, w2, m2, a2);
    }catch (Exception e) {
      RuntimeException re = new RuntimeException("bad input parameters: T [1] w [2], M [3], A [4]"
          .replace("[1]", Double.toString(logicle.T))
          .replace("[2]", Double.toString(logicle.w))
          .replace("[3]", Double.toString(logicle.M))
          .replace("[4]", Double.toString(logicle.A))
          );
      re.initCause(e);
      throw re;
    }
  }
  
  public LogicleTransform(double t2, double w2, double m2, double a2) {
    this(t2, w2, m2, a2, null);
  }
  
  public LogicleTransform(double t) {
    this(t, DEFAULT_W, DEFAULT_M, DEFAULT_A, null);
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

  private double optimizeW(double[] data) {
    /**
     * Based on the percentile method suggested by Parks/Moore.
     */
    double lowerBound = new Percentile().evaluate(data, LOGICLE_W_PERCENTILE);
    double newW;
    if (lowerBound < 0){
      newW = (logicle.M - Math.log10(logicle.T / Math.abs(lowerBound))) / 2;
    } else {
      newW = 0.2;//TODO: Reasonable?
    }
    //TODO HACKZ
    if (newW<=0) newW=0.5;
    return newW;
  }

  @Override
  public double transform(double value) {
    double newValue;
    if (value < getMinRawValue()) {
      newValue = getMinRawValue();
    } else if (value >= getMaxRawValue()) {
      newValue = getMaxRawValue();
    } else {
      newValue = value;
    }

    if (newValue >= logicle.T) {
      return 1;
    }
    return logicle.scale(newValue);
  }

  @Override
  public double inverse(double value) {
    if (value >= 1) {
      return logicle.T;
    } else {
      return logicle.inverse(value);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.logicle = new FastLogicle(logicle.T, logicle.W,logicle.M, logicle.A);
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
    return logicle.T;
  }
  
  @Override
  public String toString() {
    return getDetails();
  }


  public double[] getAxisValues() {
    return logicle.axisLabels();
  }

  public double getT() {
    return logicle.T;
  }

  public double getW() {
    return logicle.W;
  }

  public double getM() {
    return logicle.M;
  }

  public double getA() {
    return logicle.A;
  }

  @Override
  public TransformType getType() {
    return TransformType.LOGICLE;
  }

  @Override
  public String getDetails() {
    return "t=" + logicle.T + ", w="+logicle.W + ", m=" + logicle.M + ", a=" + logicle.A;
  }

  @Override
  protected AbstractTransform merge(AbstractTransform t1) {
    LogicleTransform lt = (LogicleTransform) t1;
    double newT = lt.getT() > this.getT() ? lt.getT():this.getT();
    double newW = lt.getW() > this.getW() ? lt.getW():this.getW();
    double newM = lt.getM() > this.getM() ? lt.getM():this.getM();
    double newA = lt.getA() > this.getA() ? lt.getA():this.getA();

    return new LogicleTransform(newT, newW, newM, newA);
  }
  
  @Override
  public void optimize(double[] rawData) {
    Double newT = new Percentile(LOGICLE_T_PERCENTILE).evaluate(rawData);
    double newW = optimizeW(rawData);
    double lowerbound = new Percentile(LOGICLE_W_PERCENTILE).evaluate(rawData);
    double newM = Math.log10(newT - Math.abs(lowerbound));
    double newA = newM/10;//Just an idea.
    try {
      //newT = newT >= logicle.T ? newT : logicle.T; 
      this.logicle = new FastLogicle(newT, newW, newM, newA);
    }catch (Exception e) {
      //Ideally we catch this beforehand but for now:
      RuntimeException e2 = new RuntimeException("bad input parameters: T [1] w [2], M [3], A [4]"
          .replace("[1]", Double.toString(logicle.T))
          .replace("[2]", Double.toString(newW))
          .replace("[2]", Double.toString(logicle.M))
          .replace("[2]", Double.toString(logicle.A))
          );
      //e2.initCause(e);
      
      this.logicle = new FastLogicle(logicle.T, DEFAULT_W, logicle.M, logicle.A);
    }
  }

  @Override
  protected AbstractTransform copy() {
    return new LogicleTransform(logicle.T, logicle.W, logicle.M, logicle.A);
  }
}
