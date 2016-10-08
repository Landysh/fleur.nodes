package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import java.util.ArrayList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

@SuppressWarnings("serial")
public class FCSChartPanel extends ChartPanel {
	
	ArrayList<AbstractGate> allGates = new ArrayList<AbstractGate>();
	XYBoxAnnotation selectedAnnotation;
	
	
	public FCSChartPanel(JFreeChart chart) {
		super(chart);
	}

	public void setSelectedAnnotation(XYBoxAnnotation newBox) {
		this.selectedAnnotation = newBox;
	}

	public void addGate(AbstractGate gate) {
		allGates.add(gate);
	}

}
