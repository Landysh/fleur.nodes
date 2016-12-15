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
package io.landysh.inflor.main.core.utils;

public class MatrixUtilities {

  public static double[][] pow(double[][] X, int p) {
    for (int i = 0; i < X.length; i++) {
      for (int j = 0; j < X[0].length; j++) {
        X[i][j] = Math.pow(X[i][j], p);
      }
    }
    return X;
  }

  public static double[][] transpose(double[][] X) {
    double[][] XT = new double[X[0].length][X.length];
    for (int i = 0; i < X.length; i++) {
      for (int j = 0; j < X[0].length; j++) {
        XT[j][i] = X[i][j];
      }
    }
    return XT;
  }

  public static double[] appendVectors(double[] data, double[] data2) {
    if (data == null && data2 == null) {
      return null;
    } else if (data == null && data2 != null) {
      return data2;
    } else if (data != null && data2 == null) {
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
}
