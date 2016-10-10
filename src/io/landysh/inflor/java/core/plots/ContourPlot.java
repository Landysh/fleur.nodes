package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;
import java.util.BitSet;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

import io.landysh.inflor.java.core.dataStructures.Histogram2D;
import io.landysh.inflor.java.core.utils.FCSUtils;

public class ContourPlot extends AbstractFCChart {
		
	XYPlot plot;
	ChartSpec spec;

	
	public ContourPlot(ChartSpec spec, String priorUUID) {
		super(priorUUID, spec);
		plot = new XYPlot();
		this.spec = spec;
	}

	public ContourPlot(ChartSpec spec) {
		this(spec, null);
	}

	@Override
	public void update(ChartSpec spec) {
		this.spec = spec;
	}

	@Override
	public JFreeChart createChart(double[] xData, double[] yData) {
		
		String domainName = this.spec.getDomainAxisName();
		String rangeName  = this.spec.getRangeAxisName();
		
		Histogram2D histogram = new Histogram2D(xData, spec.getXMin(), spec.getXMax(), spec.getXBinCount(), 
												yData, spec.getYMin(), spec.getYMax(), spec.getYBinCount());
		
		DefaultXYZDataset plotData = new DefaultXYZDataset();
		
		BitSet nonEmptyMask = histogram.getNonEmptyBins();
		double[] x = FCSUtils.filterColumn(nonEmptyMask, histogram.getXBins());
		double[] y = FCSUtils.filterColumn(nonEmptyMask, histogram.getYBins());
		double[] z = FCSUtils.filterColumn(nonEmptyMask, histogram.getZValues());
        //TODO: Fix this.
		//double[] zNoBorderBins = FCSUtils.filterColumn(histogram.getNonEdgeBins(), histogram.getZValues());
		PaintModel pm = new PaintModel(z);        
		double [] discreteValues = pm.getDiscreteData(z);
        plotData.addSeries("Series 1", new double[][] {x,y,discreteValues});

		
		//Renderer
		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(histogram.getXBinWidth());
        renderer.setBlockHeight(histogram.getYBinWidth());
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesVisible(0, true);
        
		Paint[] paints = pm.getPaints(); 
		double[] levels = pm.getLevels();
		LookupPaintScale paintScale = new LookupPaintScale(0,pm.getThreshold(), Color.red);
		for (int i=0;i<levels.length;i++){
			paintScale.add(levels[i], paints[i]);
		}
		renderer.setPaintScale(paintScale);
		
		//Create the plot
		plot.setDataset(plotData);
		plot.setDomainAxis(PlotUtils.createAxis(domainName, spec.getDomainTransform()));
		plot.setRangeAxis(PlotUtils.createAxis(rangeName, spec.getRangeTransform()));
		plot.setRenderer(renderer);
		//Add to panel.
		this.chart = new JFreeChart(plot);
		chart.removeLegend();
		return chart;	
	}
}
//EOF