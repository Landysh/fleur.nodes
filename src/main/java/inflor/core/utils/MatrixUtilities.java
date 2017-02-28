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
package inflor.core.utils;

public class MatrixUtilities {

  private MatrixUtilities() {}

  public static double[][] pow(double[][] x, int p) {
    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        x[i][j] = Math.pow(x[i][j], p);
      }
    }
    return x;
  }

  public static double[][] transpose(double[][] x) {
    double[][] xT = new double[x[0].length][x.length];
    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        xT[j][i] = x[i][j];
      }
    }
    return xT;
  }

  public static double[] appendVectors(double[] data, double[] data2) {
    if (data == null && data2 == null) {
      return null;
    } else if (data == null) {
      return data2;
    } else if (data2 == null) {
      return data;
    } else {
      double[] result = new double[data.length + data2.length];
      int i = 0;
      for (double d : data) {
        result[i] = d;
        i++;
      }
      for (double d : data2) {
        result[i] = d;
        i++;
      }
      return result;
    }
  }

  public static double[] flatten(double[][] spills) {
    double[] flat;
    if (spills.length > 0) {
      flat = new double[spills.length * spills[0].length];
      for (int i = 0; i < spills.length; i++) {
        for (int j = 0; j < spills[0].length; j++) {
          flat[i * spills[0].length + j] = spills[i][j];
        }
      }
    } else {
      flat = new double[] {};
    }
    return flat;
  }
}
