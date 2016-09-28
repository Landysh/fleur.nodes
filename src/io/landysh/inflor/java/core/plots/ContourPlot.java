package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

import io.landysh.inflor.java.core.dataStructures.Histogram2D;

public class ContourPlot extends AbstractFCPlot {
	
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
		
		plotData.addSeries("Series 1", new double[][] {histogram.getXBins(), 
			                                          histogram.getYBins(), 
			                                          histogram.getZValues()});
		
		//Renderer
		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(histogram.getXBinWidth());
        renderer.setBlockHeight(histogram.getYBinWidth());
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesVisible(0, true);
        double zMaxHeight = histogram.getMaxHistogramHeight();
        Paint[] contourColors = createColorScale(zMaxHeight);
        
        
		LookupPaintScale paintScale = new LookupPaintScale(0, zMaxHeight, Color.gray);
		double [] scaleValues = new double[contourColors.length];
		double delta = (zMaxHeight)/(contourColors.length -1);
		double value = 0;
		for(int i=0; i<contourColors.length; i++){
			paintScale.add(value, contourColors[i]);
			scaleValues[i] = value;
			value = value + delta;
		}
		GrayPaintScale p2 = new GrayPaintScale(0, zMaxHeight);
		renderer.setPaintScale(p2);
		
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

	private Paint[] createColorScale(double maxHeight) {
		
		Paint[] colorScale = new Paint[(int)maxHeight+1];

		float startHue = 180/360f;
		float stopHue = 1f;
		if (colorScale.length>1){
			colorScale[0] = Color.WHITE;
			colorScale[1] = Color.getHSBColor(startHue, 0.6f, 0.6f);
			for (int i=2;i<colorScale.length;i++){
				float num = (float)(i-2)/colorScale.length;
				float delta = (stopHue-startHue);
				float hueOffset = num*delta;
				float currentHue = (stopHue - hueOffset);
				colorScale[i] = Color.getHSBColor(currentHue, 0.6f, 0.6f);
			}
		}
		return colorScale;
	}
}
//EOF