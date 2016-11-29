package io.landysh.inflor.main.core.plots;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer.FillType;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.util.ShapeUtilities;

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.dataStructures.Histogram1D;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.utils.FCSUtilities;

public class HistogramPlot extends AbstractFCChart {

  public HistogramPlot(String priorUUID, ChartSpec spec) {
    super(priorUUID, spec);
    this.spec = spec;
  }

  public HistogramPlot(ChartSpec spec) {
    this(null, spec);
  }

  @Override
  public void setSpec(ChartSpec spec) {
    // TODO Auto-generated method stub
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
    JFreeChart chart = new JFreeChart(plot);
    return chart;
  }
}
