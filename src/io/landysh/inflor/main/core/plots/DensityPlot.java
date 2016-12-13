package io.landysh.inflor.main.core.plots;

import java.util.BitSet;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

import com.google.common.primitives.Doubles;

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.dataStructures.Histogram2D;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.utils.FCSUtilities;

public class DensityPlot extends AbstractFCChart {

  ChartSpec spec;
  private ColorSchemes colorScheme = ChartingDefaults.DEFAULT_COLOR_SCHEME;
  private XYPlot plot;
  private Histogram2D histogram;
  

  public DensityPlot(ChartSpec spec, String priorUUID) {
    super(priorUUID, spec);
    this.spec = spec;
  }

  public DensityPlot(ChartSpec spec) {
    this(spec, null);
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
    double domainMin = domainTransform.getMinTranformedValue();
    double domainMax = domainTransform.getMaxTransformedValue();

    AbstractTransform rangeTransform;
    FCSDimension rangeDimension = FCSUtilities.findCompatibleDimension(data, spec.getRangeAxisName());
    
    if (rangeDimension.getPreferredTransform() != null) {
      rangeTransform = rangeDimension.getPreferredTransform();
    } else {
      rangeTransform = PlotUtils.createDefaultTransform(rangeDimension.getShortName());
    }
    
    double[] rangeData = rangeTransform.transform(rangeDimension.getData());
    double rangeMin = rangeTransform.getMinTranformedValue();
    double rangeMax = rangeTransform.getMaxTransformedValue();
    histogram = new Histogram2D(domainData, domainMin, domainMax, rangeData, rangeMin, rangeMax);

    DefaultXYZDataset plotData = new DefaultXYZDataset();


    BitSet nonEmptyMask = histogram.getNonEmptyBins();
    double[] x = FCSUtilities.filterColumn(nonEmptyMask, histogram.getXBins());
    double[] y = FCSUtilities.filterColumn(nonEmptyMask, histogram.getYBins());
    double[] z = FCSUtilities.filterColumn(nonEmptyMask, histogram.getZValues());
    plotData.addSeries(data.toString(), new double[][] {x, y, z});

    XYBlockRenderer renderer = updateRenderer(histogram);

    plot = new XYPlot();
    // Create the plot
    plot.setDataset(plotData);
    plot.setDomainAxis(PlotUtils.createAxis(domainDimension.getDisplayName(), domainTransform));
    plot.setRangeAxis(PlotUtils.createAxis(rangeDimension.getDisplayName(), rangeTransform));
    plot.setRenderer(renderer);
    // Add to panel.
    JFreeChart chart = new JFreeChart(plot);
    chart.removeLegend();
    return chart;
  }

  private XYBlockRenderer updateRenderer(Histogram2D histogram) {
    BitSet nonEmptyMask = histogram.getNonEmptyBins();
    double[] z = FCSUtilities.filterColumn(nonEmptyMask, histogram.getZValues());
    LookupPaintScale paintScale = PlotUtils.createPaintScale(0, Doubles.max(z), colorScheme);
    
    
    // Renderer configuration
    XYBlockRenderer renderer = new XYBlockRenderer();
    renderer.setBlockWidth(histogram.getXBinWidth());
    renderer.setBlockHeight(histogram.getYBinWidth());
    renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
    renderer.setSeriesVisible(0, true);
    renderer.setPaintScale(paintScale);
    return renderer;
  }

  public void updateColorScheme(ColorSchemes newScheme) {
    this.colorScheme = newScheme;
    plot.setRenderer(updateRenderer(histogram));
  }
}
