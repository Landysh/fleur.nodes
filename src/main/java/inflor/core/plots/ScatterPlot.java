package main.java.inflor.core.plots;

import java.awt.Color;
import java.awt.Rectangle;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.utils.FCSUtilities;

public class ScatterPlot extends AbstractFCChart {

  public ScatterPlot(String priorUUID, ChartSpec spec) {
    super(priorUUID, spec);
  }

  public ScatterPlot(ChartSpec plotSpec) {
    this(null, plotSpec);
  }

  @Override
  public JFreeChart createChart(FCSFrame data) {
    FCSDimension domainDimension = FCSUtilities.findCompatibleDimension(data, spec.getDomainAxisName());
    AbstractTransform domainTransform;
    if (domainDimension.getPreferredTransform() != null) {
      domainTransform = domainDimension.getPreferredTransform();
    } else {
      domainTransform = PlotUtils.createDefaultTransform(domainDimension.getShortName());
    }
    double[] domainData = domainTransform.transform(domainDimension.getData());


    AbstractTransform rangeTransform;
    FCSDimension rangeDimension = FCSUtilities.findCompatibleDimension(data, spec.getRangeAxisName());
    
    if (rangeDimension.getPreferredTransform() != null) {
      rangeTransform = rangeDimension.getPreferredTransform();
    } else {
      rangeTransform = PlotUtils.createDefaultTransform(rangeDimension.getShortName());
    }
    
    double[] rangeData = rangeTransform.transform(rangeDimension.getData());

    DefaultXYDataset plotData = new DefaultXYDataset();
    double[][] seriesArray = new double[2][domainData.length];
    for (int i=0;i<domainData.length;i++){
      seriesArray[0][i] = domainData[i];
      seriesArray[1][i] = rangeData[i];
    }
    plotData.addSeries("l1", seriesArray);
    XYPlot plot = new XYPlot();
    // Create the plot
    plot.setDataset(plotData);
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesPaint(0, Color.GRAY);
    Rectangle rect = new Rectangle(1, 1);
    renderer.setSeriesShape(0, rect);
    renderer.setSeriesLinesVisible(0, false);
    plot.setRenderer(renderer);
    plot.setDomainAxis(PlotUtils.createAxis(domainDimension.getDisplayName(), domainTransform));
    plot.setRangeAxis(PlotUtils.createAxis(rangeDimension.getDisplayName(), rangeTransform));
    // Add to panel.
    JFreeChart chart = new JFreeChart(plot);
    chart.removeLegend();
    return chart;
  }

}
