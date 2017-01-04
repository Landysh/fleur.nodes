package main.java.inflor.core.compensation;

import org.apache.commons.math3.stat.descriptive.rank.Median;

public class TheilSenEstimator {
  
  private TheilSenEstimator(){}
  
  public static double evaluate(double[] x, double[] y){
    if (x!=null && x.length == y.length && x.length > 0){
      double[] ms = new double[x.length*y.length];
      for (int i=0;i<x.length;i++){
        for (int j=0;j<x.length;j++){
          ms[i*x.length + j] = (y[j]-y[i])/(x[j] - x[i]);
        }
      }
      Median median = new Median();
      
      return median.evaluate(ms);
    } else {
      throw new IllegalArgumentException("x and y must not be null and must have a length > 0");
    }
  }
}
