package io.landysh.inflor.tests;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicDisplayTransform;
import io.landysh.inflor.java.knime.nodes.createGates.ui.ScatterPlot;

@SuppressWarnings("serial")
public class UI_ScattPlotTest extends ApplicationFrame {
	   public UI_ScattPlotTest(String title) throws Exception {
		super(title);
		
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		final FCSFileReader reader = new FCSFileReader(logiclePath, false);
		reader.readData();
		final ColumnStore dataStore = reader.getColumnStore();

		PlotSpec spec = new PlotSpec(null);
		spec.setPlotType(PlotTypes.Scatter);
		spec.setDomainAxisName("FSC-A");
		spec.setRangeAxisName("SSC-A");
		spec.setxBinCount(1024);
		spec.setyBinCount(1024);
		spec.setxMin(0);
		spec.setyMin(100);
		spec.setxMax(262144);
		spec.setyMax(262144);
		
		
		spec.setRangeTransform(new LogrithmicDisplayTransform(spec.getYMin(), spec.getYMax()));
		spec.setDomainTransform(new BoundDisplayTransform(spec.getYMin(), spec.getyMax()));
		
		//final double[] fcs = { 400, 600, 300, 500, 600, 500, 800, 200, 300, 800, 900, 400, 200, 600, 400 };
		//final double[] ssc = { 300, 300, 600, 200, 800, 500, 600, 400, 100, 200, 400, 800, 900, 700, 500 };
		
		double[] X = dataStore.getColumn(spec.getDomainAxisName());
		double[] Y = dataStore.getColumn(spec.getRangeAxisName());

		ScatterPlot plot = new ScatterPlot(spec);
		JFreeChart chart = plot.createChart(X, Y);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(256,256));
		panel.setRangeZoomable(true);
		panel.setDomainZoomable(true);

		this.getContentPane().add(panel);
	}

	public static void main(String[] args) throws Exception {
		UI_ScattPlotTest test = new UI_ScattPlotTest("ContourPlotTest");
			  test.pack();
		      test.setVisible(true);
	}	   
}
//EOF