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
