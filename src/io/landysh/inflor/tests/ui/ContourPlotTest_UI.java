package io.landysh.inflor.tests.ui;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.ui.GateCreationToolBar;

@SuppressWarnings("serial")
public class ContourPlotTest_UI extends ApplicationFrame {
	
	private JToolBar toolbar;
	private FCSChartPanel panel;
	MouseInputListener activeListener;
	
	public ContourPlotTest_UI(String title) throws Exception {
		super(title);
		//Setup data
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		final ColumnStore dataStore = FCSFileReader.read(logiclePath, false);

		ChartSpec spec = new ChartSpec();
		spec.setPlotType(PlotTypes.Contour);
		spec.setDomainAxisName("FSC-A");
		spec.setRangeAxisName("SSC-A");
		spec.setxBinCount(256);
		spec.setyBinCount(256);
		spec.setxMin(0);
		spec.setxMax(262144);
		spec.setyMin(0);
		spec.setyMax(50000);
		
		spec.setDomainTransform(new BoundDisplayTransform(spec.getXMin(), spec.getXMax()));
		spec.setRangeTransform(new BoundDisplayTransform(spec.getYMin(), spec.getyMax()));
		
		double[] X = dataStore.getDimensionData(spec.getDomainAxisName());
		double[] Y = dataStore.getDimensionData(spec.getRangeAxisName());

		
		//create the plot
		ContourPlot plot = new ContourPlot(spec);
		JFreeChart chart = plot.createChart(X, Y);
		panel = new FCSChartPanel(chart, dataStore);		
		
	    
	    JPanel editorPanel = new JPanel();
	    editorPanel.add(panel);
	    editorPanel.add(new GateCreationToolBar(panel));
	    
		this.getContentPane().add(editorPanel);
	}

	public static void main(String[] args) throws Exception {
		ContourPlotTest_UI test = new ContourPlotTest_UI("ContourPlotTest");
		test.pack();
		test.setVisible(true);
	}	   
}
//EOF