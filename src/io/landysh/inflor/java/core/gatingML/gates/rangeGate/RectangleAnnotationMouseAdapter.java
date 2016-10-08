package io.landysh.inflor.java.core.gatingML.gates.rangeGate;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.ui.DefaultGraphics;

public class RectangleAnnotationMouseAdapter extends MouseAdapter {
	
	private XYBoxAnnotation annotation;
	private FCSChartPanel panel;
	private double x0;
	private double x1;
	private double y0;
	private double y1;


	public RectangleAnnotationMouseAdapter(FCSChartPanel panel, XYBoxAnnotation box, double x0, double x1, double y0, double y1) {
		this.panel = panel;
		this.annotation = box;
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}

	@Override
	public void mouseClicked(MouseEvent e){
		XYPlot plot = (XYPlot) panel.getChart().getPlot();
		plot.removeAnnotation(annotation);
		XYBoxAnnotation newBox = new XYBoxAnnotation(x0, y0, x1, y1, 
													 DefaultGraphics.SELECTED_STROKE, 
													 DefaultGraphics.DEFAULT_GATE_COLOR);
		plot.addAnnotation(newBox);
	}
	
	@Override
	public void mouseDragged(MouseEvent e){
		
	}
	
}
