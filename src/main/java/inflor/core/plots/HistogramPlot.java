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
package main.java.inflor.core.plots;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer.FillType;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.util.ShapeUtilities;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.data.Histogram1D;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.utils.FCSUtilities;

public class HistogramPlot extends AbstractFCChart {

  public HistogramPlot(String priorUUID, ChartSpec spec) {
    super(priorUUID, spec);
    this.spec = spec;
  }

  public HistogramPlot(ChartSpec spec) {
    this(null, spec);
  }

  @Override
  public void setSpec(ChartSpec newSpec) {
    this.spec = newSpec;
  }

  @Override
  public JFreeChart createChart(FCSFrame dataFrame) {

    FCSDimension domainDimension =
        FCSUtilities.findCompatibleDimension(dataFrame, spec.getDomainAxisName());

    AbstractTransform transform = domainDimension.getPreferredTransform();
    double[] transformedData = transform.transform(domainDimension.getData());

    Histogram1D hist = new Histogram1D(transformedData, transform.getMinTranformedValue(),
        transform.getMaxTransformedValue(), ChartingDefaults.BIN_COUNT);

    DefaultXYDataset dataset = new DefaultXYDataset();
    dataset.addSeries(dataFrame.getPrefferedName(), hist.getData());

    ValueAxis domainAxis = PlotUtils.createAxis(domainDimension.getDisplayName(), transform);
    ValueAxis rangeAxis = new NumberAxis(spec.getRangeAxisName());
    FillType fillType = FillType.TO_ZERO;
    XYItemRenderer renderer = new XYSplineRenderer(1, fillType);
    renderer.setSeriesShape(0, ShapeUtilities.createDiamond(Float.MIN_VALUE));// Make the points
                                                                              // invisible
    XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
    return new JFreeChart(plot);
  }
}
