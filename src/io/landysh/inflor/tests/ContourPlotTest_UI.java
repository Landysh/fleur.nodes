package io.landysh.inflor.tests;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.plots.BoundDisplayTransform;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;

@SuppressWarnings("serial")
public class ContourPlotTest_UI extends ApplicationFrame {
	   public ContourPlotTest_UI(String title) throws Exception {
		super(title);
		PlotSpec spec = new PlotSpec(null);
		spec.setPlotType(PlotTypes.Contour);
		spec.setHorizontalAxis("FSC");
		spec.setVerticalAxis("SSC");
		spec.setxBinCount(5);
		spec.setyBinCount(5);
		spec.setxMin(0);
		spec.setyMin(0);
		spec.setxMax(1000);
		spec.setyMax(1000);

		spec.setRangeTransform(new BoundDisplayTransform(spec.getxMin(), spec.getxMax()));
		spec.setDomainTransform(new BoundDisplayTransform(spec.getyMin(), spec.getyMax()));
		
		final double[] fcs = { 400, 600, 300, 500, 600, 500, 800, 200, 300, 800, 900, 400, 200, 600, 400 };
		final double[] ssc = { 300, 300, 600, 200, 800, 500, 600, 400, 100, 200, 400, 800, 900, 700, 500 };
		
		ContourPlot plot = new ContourPlot(spec);
		JFreeChart chart = plot.createChart(fcs, ssc);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(256,256));
		panel.setRangeZoomable(false);
		panel.setDomainZoomable(false);

		this.getContentPane().add(panel);
	}

	public static void main(String[] args) throws Exception {
		ContourPlotTest_UI test = new ContourPlotTest_UI("ContourPlotTest");
			  test.pack();
		      test.setVisible(true);
	}	   
}
//EOF