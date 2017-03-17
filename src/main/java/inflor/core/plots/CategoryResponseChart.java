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
package inflor.core.plots;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.ui.RectangleAnchor;

import com.google.common.primitives.Doubles;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.data.Histogram1D;
import inflor.core.transforms.AbstractTransform;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.PlotUtils;
import inflor.knime.core.NodeUtilities;

public class CategoryResponseChart {

  private AbstractTransform transform;
  private String axisName;

  public CategoryResponseChart(String domainName, AbstractTransform domainTransform) {
    this.transform = domainTransform;
    this.axisName = domainName;
  }

  public JFreeChart createChart(FCSFrame dataFrame) {

    CategoryXYZDataSet categoryData = new CategoryXYZDataSet();
    double zMin = Double.MAX_VALUE;
    double zMax = 1;
    if (dataFrame.getKeywords().containsKey(FCSUtilities.KEY_MERGE_MAP)) {
      String[] mergeMap = dataFrame.getKeywordValue(FCSUtilities.KEY_MERGE_MAP)
          .split(NodeUtilities.DELIMITER_REGEX);
      FCSDimension dim = dataFrame.getDimension(axisName);
      double[] transformedData = transform.transform(dim.getData());
      int perFileSize = dim.getData().length / mergeMap.length;
      for (int i = 0; i < mergeMap.length; i++) {

        double[] unMergedData = new double[perFileSize];
        for (int j = 0; j < unMergedData.length; j++) {
          unMergedData[j] = transformedData[i * unMergedData.length + j];
        }

        Histogram1D hist = new Histogram1D(unMergedData, transform.getMinTranformedValue(),
            transform.getMaxTransformedValue(), ChartingDefaults.BIN_COUNT);
        double[] x = hist.getNonZeroX();
        double[] y = new double[x.length];
        for (int j = 0; j < y.length; j++) {
          y[j] = i;
        }
        double[] z = hist.getNonZeroY();
        double currentZMin = Doubles.min(z);
        double currentZMax = Doubles.max(z);
        if (currentZMin < zMin) {
          zMin = currentZMin;
        } else if (currentZMax > zMax) {
          zMax = currentZMax;
        }

        categoryData.addCategoricalSeries(mergeMap[i], x, z);
      }
    } else {
      FCSDimension dim = dataFrame.getDimension(axisName);
      double[] transformedData = transform.transform(dim.getData());
      Histogram1D hist = new Histogram1D(transformedData, transform.getMinTranformedValue(),
          transform.getMaxTransformedValue(), ChartingDefaults.BIN_COUNT);
      double[] x = hist.getNonZeroX();
      double[] y = new double[x.length];
      for (int j = 0; j < y.length; j++) {
        y[j] = 0;
      }
      double[] z = hist.getNonZeroY();
      double currentZMin = Doubles.min(z);
      double currentZMax = Doubles.max(z);
      if (currentZMin < zMin) {
        zMin = currentZMin;
      } else if (currentZMax > zMax) {
        zMax = currentZMax;
      }

      categoryData.addCategoricalSeries(dataFrame.getDisplayName(), x, z);
    }

    ValueAxis domainAxis = PlotUtils.createAxis(axisName, transform);
    NumberAxis rangeAxis = new CategoricalNumberAxis("", categoryData.getLabelMap());
    // Renderer configuration
    XYBlockRenderer renderer = new XYBlockRenderer();
    double xWidth = (transform.getMaxTransformedValue() - transform.getMinTranformedValue())
        / ChartingDefaults.BIN_COUNT;
    renderer.setBlockWidth(xWidth);
    renderer.setBlockHeight(0.5);
    renderer.setBlockAnchor(RectangleAnchor.LEFT);

    PaintScale paintScale = PlotUtils.createPaintScale(zMax, ChartingDefaults.DEFAULT_COLOR_SCHEME);
    renderer.setPaintScale(paintScale);

    // Add to panel.
    XYPlot responsePlot = new XYPlot(categoryData, domainAxis, rangeAxis, renderer);
    JFreeChart chart = new JFreeChart(responsePlot);
    chart.removeLegend();
    return chart;
  }
}
