/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.transforms;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class BoundDisplayTransform extends AbstractTransform {

  private static final long serialVersionUID = 1L;
  private static final double LOWER_BOUND_PERCENT = 1;
  private static final double UPPER_BOUND_PERCENT = 99;

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

  @Override
  public double[] transform(double[] rawData) {
    double[] transformedData = new double[rawData.length];
    for (int i = 0; i < rawData.length; i++) {
      transformedData[i] = transform(rawData[i]);
    }
    return transformedData;
  }

  @Override
  public double getMinTranformedValue() {
    return boundaryMin;
  }

  public double getMaxValue() {
    return boundaryMax;
  }

  @Override
  public double transform(double value) {
    double tValue;
    if (value < boundaryMin) {
      tValue = boundaryMin;
    } else if (value > boundaryMax) {
      tValue = boundaryMax;
    } else {
      tValue = value;
    }
    return tValue;
  }

  @Override
  public double inverse(double value) {
    return value;
  }

  @Override
  public double getMaxTransformedValue() {
    return boundaryMax;
  }

  @Override
  public double getMinRawValue() {
    return boundaryMin;
  }

  @Override
  public double getMaxRawValue() {
    return boundaryMax;
  }

  @Override
  public String toString() {
    return getDetails();
  }

  public void optimize(double[] data) {
    this.boundaryMin = new Percentile().evaluate(data, LOWER_BOUND_PERCENT);
    this.boundaryMax = new Percentile().evaluate(data, UPPER_BOUND_PERCENT);
  }

  @Override
  public TransformType getType() {
    return TransformType.BOUNDARY;
  }

  @Override
  public String getDetails() {
    return "Min display: " + boundaryMin + ", Max display: " + boundaryMax;
  }

}
