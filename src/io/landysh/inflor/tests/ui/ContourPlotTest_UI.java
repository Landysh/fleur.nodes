package io.landysh.inflor.tests.ui;

import java.awt.Dimension;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicDisplayTransform;

@SuppressWarnings("serial")
public class ContourPlotTest_UI extends ApplicationFrame {
	   public ContourPlotTest_UI(String title) throws Exception {
		super(title);
		
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		final FCSFileReader reader = new FCSFileReader(logiclePath, false);
		reader.readData();
		final ColumnStore dataStore = reader.getColumnStore();

		ChartSpec spec = new ChartSpec();
		spec.setPlotType(PlotTypes.Contour);
		spec.setDomainAxisName("FSC-A");
		spec.setRangeAxisName("SSC-A");
		spec.setxBinCount(1024);
		spec.setyBinCount(1024);
		spec.setxMin(0);
		spec.setyMin(1000);
		spec.setxMax(262144);
		spec.setyMax(262144);
		
		
		spec.setRangeTransform(new LogrithmicDisplayTransform(spec.getYMin(), spec.getYMax()));
		spec.setDomainTransform(new BoundDisplayTransform(spec.getYMin(), spec.getyMax()));
		
		double[] X = dataStore.getDimensionData(spec.getDomainAxisName());
		double[] Y = dataStore.getDimensionData(spec.getRangeAxisName());

		ContourPlot plot = new ContourPlot(spec);
		JFreeChart chart = plot.createChart(X, Y);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(256,256));
		panel.setRangeZoomable(true);
		panel.setDomainZoomable(true);

		this.getContentPane().add(panel);
	}

	public static void main(String[] args) throws Exception {
		ContourPlotTest_UI test = new ContourPlotTest_UI("ContourPlotTest");
			  test.pack();
		      test.setVisible(true);
	}	   
}
//EOF