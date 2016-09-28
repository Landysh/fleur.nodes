package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.FastScatterPlot;

import io.landysh.inflor.java.core.plots.AbstractFCPlot;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotUtils;

public class ScatterPlot extends AbstractFCPlot {
	
	FastScatterPlot plot = new FastScatterPlot();
	float[][] fastData;
	
	public ScatterPlot(ChartSpec spec, String priorUUID) {
		super(priorUUID, spec);
	}
	
	public ScatterPlot(ChartSpec spec) {
		super(null, spec);
	}

	@Override
	public void update(ChartSpec spec) {
		// TODO Auto-generated method stub

	}

	@Override
	public JFreeChart createChart(double[] xData, double[] yData) {

		
		fastData = createFastData(xData, 
								  yData);
		
		String domainName = this.spec.getDomainAxisName();
		String rangeName  = this.spec.getRangeAxisName();
		
		//Create the plot
		plot.setData(fastData);
		plot.setPaint(Color.BLACK);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainAxis(PlotUtils.createAxis(domainName, spec.getDomainTransform()));
		plot.setRangeAxis(PlotUtils.createAxis(rangeName, spec.getRangeTransform()));
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);

		
		//Add to panel.
		this.chart = new JFreeChart(plot);
		chart.removeLegend();
		return chart;
		
	}

	private float[][] createFastData(double[] slowDomainData, double[] slowRangeData) {
		float[] domainData = new float[slowDomainData.length];
		float[] rangeData  = new float[slowDomainData.length];
		for (int i=0;i<slowDomainData.length;i++){
			domainData[i] = (float) slowDomainData[i];
			rangeData[i] = (float) slowRangeData[i];
		}
		return new float[][] {domainData, rangeData};
	}
}
