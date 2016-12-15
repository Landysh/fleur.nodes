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
package io.landysh.inflor.main.core.transforms;

import com.google.common.primitives.Doubles;

public class LogrithmicTransform extends AbstractTransform {


  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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
  
  @Override
  public String toString(){
    return "min: " + getMinTranformedValue() + ", max " + getMaxTransformedValue() + " @" + getID();
  }

  public void optimize(double[] data) {
    min = Doubles.min(data);
    max = Doubles.max(data);
  }
}
