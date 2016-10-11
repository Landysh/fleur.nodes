package io.landysh.inflor.java.core.plots;

import java.util.ArrayList;
import java.util.BitSet;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RangeGate;
import io.landysh.inflor.java.core.utils.BitSetUtils;

@SuppressWarnings("serial")
public class FCSChartPanel extends ChartPanel {
	
	ArrayList<AbstractGate> allGates = new ArrayList<AbstractGate>();
	ColumnStore data;
	XYBoxAnnotation selectedAnnotation;
	
	
	public FCSChartPanel(JFreeChart chart, ColumnStore data) {
		super(chart);
		this.data = data;
	}

	public void setSelectedAnnotation(XYBoxAnnotation newBox) {
		this.selectedAnnotation = newBox;
	}

	public String addRectangleGate(RangeGate gate) {
		BitSet mask = gate.evaluate(data);		
		allGates.add(gate);
		String result = BitSetUtils.frequencyOfParent(mask, 2);
		String label = gate.toString() + ": " + result;
		return label;
	}
}
