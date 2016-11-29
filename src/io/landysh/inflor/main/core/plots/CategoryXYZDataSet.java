package io.landysh.inflor.main.core.plots;

import java.util.HashMap;

import org.jfree.data.xy.DefaultXYZDataset;

@SuppressWarnings("serial")
public class CategoryXYZDataSet extends DefaultXYZDataset {

  private HashMap<Integer, String> labelMap;

  public CategoryXYZDataSet() {
    super();
    labelMap = new HashMap<Integer, String>();
  }

  public synchronized void addCategoricalSeries(String name, double[] xValues, double[] zValues) {
    double[] yValues = new double[xValues.length];
    int yValue = this.getSeriesCount();
    for (int i = 0; i < yValues.length; i++) {
      yValues[i] = yValue;
    }
    labelMap.put(yValue, name);
    this.addSeries(name, new double[][] {xValues, yValues, zValues});
  }

  public HashMap<Integer, String> getLabelMap() {
    return this.labelMap;
  }
}
