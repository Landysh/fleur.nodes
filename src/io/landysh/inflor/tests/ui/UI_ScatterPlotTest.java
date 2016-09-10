package io.landysh.inflor.tests.ui;

import java.awt.Dimension;
import java.util.Random;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.knime.nodes.createGates.ui.ScatterPlot;

@SuppressWarnings("serial")
public class UI_ScatterPlotTest extends ApplicationFrame {

	public UI_ScatterPlotTest(String title) throws Exception {
		super(title);
		PlotSpec spec = new PlotSpec(null);
		spec.setPlotType(PlotTypes.Contour);
		spec.setDomainAxisName("FSC");
		spec.setRangeAxisName("SSC");
		spec.setxBinCount(5);
		spec.setyBinCount(5);
		spec.setxMin(0);
		spec.setyMin(0);
		spec.setxMax(1);
		spec.setyMax(1);

		spec.setRangeTransform(new BoundDisplayTransform(spec.getxMin(), spec.getxMax()));
		spec.setDomainTransform(new BoundDisplayTransform(spec.getyMin(), spec.getyMax()));
		
		int size = 100000;
		Random rando = new Random();
		double[] xRando = new double[size];
		double[] yRando = new double[size];
		for (int i=0;i<size;i++){
			xRando[i] = rando.nextFloat();
			yRando[i] = rando.nextFloat();
		}
		
		final double[] fcs = { 400, 600, 300, 500, 600, 500, 800, 200, 300, 800, 900, 400, 200, 600, 400 };
		final double[] ssc = { 300, 300, 600, 200, 800, 500, 600, 400, 100, 200, 400, 800, 900, 700, 500 };

		ScatterPlot plot = new ScatterPlot(spec);
		JFreeChart chart = plot.createChart(xRando, yRando);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(256, 256));
		panel.setRangeZoomable(false);
		panel.setDomainZoomable(false);

		this.getContentPane().add(panel);
	}

	public static void main(String[] args) throws Exception {
		UI_ScatterPlotTest test = new UI_ScatterPlotTest("ScatterPlotTest");
		test.pack();
		test.setVisible(true);
	}
}
// EOF