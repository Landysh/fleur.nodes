package io.landysh.inflor.tests.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.plots.gateui.GateCreationToolBar;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.utils.FCSUtils;

@SuppressWarnings("serial")
public class HistogramPlotTest extends ApplicationFrame {
	
	private FCSChartPanel panel;
	MouseInputListener activeListener;
	private GateCreationToolBar toolbar;
	
	public HistogramPlotTest(String title) throws Exception {
		super(title);
		//Setup data
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		String bigPath = "src/io/landysh/inflor/tests/extData/20mbFCS3.fcs";
		final ColumnStore dataStore = FCSFileReader.read(logiclePath, false);

		ChartSpec spec = new ChartSpec();
		spec.setPlotType(PlotTypes.Histogram);
		spec.setDomainAxisName("FSC-A");
		spec.setDomainTransform(new LogicleTransform());
		spec.setRangeAxisName("Count");
		FCSDimension X = FCSUtils.findCompatibleDimension(dataStore, spec.getDomainAxisName());
		FCSDimension Y = FCSUtils.findCompatibleDimension(dataStore, spec.getRangeAxisName());//Should be null.

		HistogramPlot plot = new HistogramPlot(spec);
		JFreeChart chart = plot.createChart(X, Y);
		panel = new FCSChartPanel(chart, dataStore);
		toolbar = new GateCreationToolBar(panel);
		panel.setSelectionListener(toolbar.getSelectionListener());
	    JPanel editorPanel = new JPanel();
	    editorPanel.add(panel);
	    editorPanel.add(toolbar);
		this.getContentPane().add(editorPanel);

	}

	public static void main(String[] args) throws Exception {
		HistogramPlotTest test = new HistogramPlotTest("ContourPlotTest");
		test.pack();
		test.setVisible(true);
	}	   
}