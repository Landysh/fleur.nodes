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

public class BoundDisplayTransform extends AbstractTransform {

  private static final long serialVersionUID = 1L;
  private double boundaryMin;
  private double boundaryMax;

  /**
   * @param min - this minimum value to be shown on the plot.
   * @param max - this maximum value visible on the displayed plot.
   * @param roundOutliers - Whether or not to round outliers. Important for gatiingML and (literal)
   *        edge cases.
   */

  public BoundDisplayTransform(double min, double max, String priorUUID) {
    super(priorUUID);
    this.boundaryMin = min;
    this.boundaryMax = max;
    }
  
  public BoundDisplayTransform(double min, double max) {
    this(min, max, null);
  }

  public BoundDisplayTransform() {
    this(0,262144,null);
  }

  @Override
  public double[] transform(double[] rawData) {
    double[] transformedData = new double[rawData.length];
    for (int i = 0; i < rawData.length; i++) {
      transformedData[i] = transform(rawData[i]);
    }
    return transformedData;
  }

  public double getMinTranformedValue() {
    return boundaryMin;
  }

  public double getMaxValue() {
    return boundaryMax;
  }

  @Override
  public double transform(double d) {
    if (d < boundaryMin) {
      d = boundaryMin;
    } else if (d > boundaryMax) {
      d = boundaryMax;
    } else {
      // noop
    }
    return d;
  }

  @Override
  public double inverse(double value) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double getMaxTransformedValue() {
    // TODO Auto-generated method stub
    return boundaryMax;
  }

  @Override
  public double getMinRawValue() {
    // TODO Auto-generated method stub
    return -1;
  }

  @Override
  public double getMaxRawValue() {
    // TODO Auto-generated method stub
    return -1;
  }

  @Override
  public String toString() {
    return "BMin " + boundaryMin + ", BMax " + boundaryMax;
  }
  
  public void optimize(double[] data) {
    this.boundaryMin = Doubles.min(data);
    this.boundaryMax = Doubles.max(data);
  }
  
}