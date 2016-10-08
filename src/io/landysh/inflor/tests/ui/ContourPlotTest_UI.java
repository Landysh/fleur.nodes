package io.landysh.inflor.tests.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.FCSChartPanel;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RectangleGateListener;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;

@SuppressWarnings("serial")
public class ContourPlotTest_UI extends ApplicationFrame {
	
	private static final String TOOLBAR_TITLE = "Cursor Toolbar";
	ArrayList<JButton> cursorButtons;
	private JToolBar toolbar;
	private FCSChartPanel panel;
	MouseInputListener activeListener;
	
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
		spec.setxBinCount(128);
		spec.setyBinCount(128);
		spec.setxMin(0);
		spec.setxMax(262144);
		spec.setyMin(0);
		spec.setyMax(262144);
		
		spec.setDomainTransform(new BoundDisplayTransform(spec.getXMin(), spec.getXMax()));
		spec.setRangeTransform(new BoundDisplayTransform(spec.getYMin(), spec.getyMax()));
		
		double[] X = dataStore.getDimensionData(spec.getDomainAxisName());
		double[] Y = dataStore.getDimensionData(spec.getRangeAxisName());

		ContourPlot plot = new ContourPlot(spec);
		JFreeChart chart = plot.createChart(X, Y);
		panel = new FCSChartPanel(chart);
		panel.setRangeZoomable(false);
		panel.setDomainZoomable(false);
	    panel.setBackground(Color.WHITE);
		createCursorButtons();
		
		
	    
	    JPanel editorPanel = new JPanel();
	    editorPanel.add(panel);
	    editorPanel.add(toolbar);
	    
		this.getContentPane().add(editorPanel);
	}

	private void createCursorButtons() {
	    JButton zoomButton = new JButton("Default");
	    zoomButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (JButton button:cursorButtons){
					button.setEnabled(true);
					((JButton)arg0.getSource()).setEnabled(false);
				}
				
			}});
	    
	    JButton rectGateButton = new JButton("RectGate");
	    
	    ActionListener listener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				activeListener = new RectangleGateListener(panel);
				panel.addMouseListener(activeListener);
				panel.addMouseMotionListener(activeListener);
				
			}};
	    
	    rectGateButton.addActionListener(listener);
	    
	    cursorButtons = new ArrayList<JButton>();
	    cursorButtons.add(zoomButton);
	    cursorButtons.add(rectGateButton);    
	    toolbar = new JToolBar(TOOLBAR_TITLE);	   
	    cursorButtons.forEach(button-> toolbar.add(button));
	}

	public static void main(String[] args) throws Exception {
		ContourPlotTest_UI test = new ContourPlotTest_UI("ContourPlotTest");
		test.pack();
		test.setVisible(true);
	}	   
}
//EOF