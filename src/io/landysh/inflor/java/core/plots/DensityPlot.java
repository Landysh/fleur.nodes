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

import com.google.common.primitives.Doubles;

import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.dataStructures.Histogram2D;
import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.core.utils.FCSUtils;

public class DensityPlot extends AbstractFCChart {
		
	ChartSpec spec;
	private ColorSchemes colorScheme = ColorSchemes.COOL_HEATMAP;

	public DensityPlot(ChartSpec spec, String priorUUID) {
		super(priorUUID, spec);
		this.spec = spec;
	}

	public DensityPlot(ChartSpec spec) {
		this(spec, null);
	}
	
	@Override
	public JFreeChart createChart(FCSDimension domainDimension, FCSDimension rangeDimension) {
		AbstractTransform domainTransform = spec.getDomainTransform();
		AbstractTransform rangeTransform = spec.getRangeTransform();
		double[] domainData = domainTransform.transform(domainDimension.getData());
		double domainMin = domainTransform.transform(Doubles.min(domainData));
		double domainMax = domainTransform.transform(domainDimension.getRange());
		double[] rangeData = rangeTransform.transform(rangeDimension.getData());
		double rangeMin = rangeTransform.transform(Doubles.min(rangeData));
		double rangeMax = rangeTransform.transform(rangeDimension.getRange());
		Histogram2D histogram = new Histogram2D(domainData, domainMin, domainMax, 
				rangeData, rangeMin, rangeMax);

		DefaultXYZDataset plotData = new DefaultXYZDataset();
		
		BitSet nonEmptyMask = histogram.getNonEmptyBins();
		double[] x = FCSUtils.filterColumn(nonEmptyMask, histogram.getXBins());
		double[] y = FCSUtils.filterColumn(nonEmptyMask, histogram.getYBins());
		double[] z = FCSUtils.filterColumn(nonEmptyMask, histogram.getZValues());
        //TODO: Fix this.
		//double[] zNoBorderBins = FCSUtils.filterColumn(histogram.getNonEdgeBins(), histogram.getZValues());
		LookupPaintScale paintScale =  createPaintScale(x,y,z, colorScheme);
		
		//Renderer configuration
		XYBlockRenderer renderer = new XYBlockRenderer();
		renderer.setBlockWidth(histogram.getXBinWidth());
        renderer.setBlockHeight(histogram.getYBinWidth());
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesVisible(0, true);
		renderer.setPaintScale(paintScale);
		
		XYPlot plot = new XYPlot();
		//Create the plot
		plot.setDataset(plotData);
		plot.setDomainAxis(PlotUtils.createAxis(domainDimension.getDisplayName(), domainTransform));
		plot.setRangeAxis(PlotUtils.createAxis(rangeDimension.getDisplayName(), rangeTransform));
		plot.setRenderer(renderer);
		//Add to panel.
		JFreeChart chart = new JFreeChart(plot);
		chart.removeLegend();
		return chart;	
	}

	private LookupPaintScale createPaintScale(double[] x, double[] y, double[] z, ColorSchemes scheme) {
		PaintModel pm = new PaintModel(scheme, z);        
		double [] discreteValues = pm.getDiscreteData(z);
        DefaultXYZDataset plotData = new DefaultXYZDataset();
		plotData.addSeries("", new double[][] {x,y,discreteValues});
		Paint[] paints = pm.getPaints(); 
		double[] levels = pm.getLevels();
		LookupPaintScale paintScale = new LookupPaintScale(0,pm.getThreshold(), Color.red);
		for (int i=0;i<levels.length;i++){
			paintScale.add(levels[i], paints[i]);
		}
		return paintScale;
	}
	
	public void updateColorScheme(ColorSchemes newScheme){
		this.colorScheme = newScheme;
	}
}
