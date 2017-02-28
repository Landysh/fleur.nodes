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
package inflor.core.plots;

import java.util.BitSet;
import java.util.Optional;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

import com.google.common.primitives.Doubles;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.data.Histogram2D;
import inflor.core.transforms.AbstractTransform;
import inflor.core.utils.FCSUtilities;

public class DensityPlot extends AbstractFCChart {

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
    Optional<FCSDimension> domainDimension = FCSUtilities.findCompatibleDimension(data, spec.getDomainAxisName());
    AbstractTransform domainTransform;
    if (domainDimension.isPresent()) {
      domainTransform = domainDimension.get().getPreferredTransform();
    } else {
      domainTransform = PlotUtils.createDefaultTransform(domainDimension.get().getShortName());
    }
    double[] domainData = domainTransform.transform(domainDimension.get().getData());
    double domainMin = domainTransform.getMinTranformedValue();
    double domainMax = domainTransform.getMaxTransformedValue();

    AbstractTransform rangeTransform;
    Optional<FCSDimension> rangeDimension = FCSUtilities.findCompatibleDimension(data, spec.getRangeAxisName());
    
    if (rangeDimension.isPresent()) {
      rangeTransform = rangeDimension.get().getPreferredTransform();
    } else {
      rangeTransform = PlotUtils.createDefaultTransform(rangeDimension.get().getShortName());
    }
    
    double[] rangeData = rangeTransform.transform(rangeDimension.get().getData());
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
    plot.setDomainAxis(PlotUtils.createAxis(rangeDimension.get().getDisplayName(), domainTransform));
    plot.setRangeAxis(PlotUtils.createAxis(rangeDimension.get().getDisplayName(), rangeTransform));
    plot.setRenderer(renderer);
    // Add to panel.
    JFreeChart chart = new JFreeChart(plot);
    chart.removeLegend();
    return chart;
  }

  private XYBlockRenderer updateRenderer(Histogram2D histogram) {
    BitSet nonEmptyMask = histogram.getNonEmptyBins();
    double[] z = FCSUtilities.filterColumn(nonEmptyMask, histogram.getZValues());
    LookupPaintScale paintScale = PlotUtils.createPaintScale(Doubles.max(z), colorScheme);
    
    
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
