package io.landysh.inflor.java.core.plots.gateui;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.ui.DefaultGraphics;
import io.landysh.inflor.java.core.utils.ChartUtils;

public class GateSelectionAdapter extends MouseAdapter {
	
	private XYGateAnnotation selectedAnnotation;
	private FCSChartPanel panel;

	public GateSelectionAdapter(FCSChartPanel panel) {
		this.panel = panel;
	}

	@Override
	public void mouseClicked(MouseEvent e){
		XYPlot plot = panel.getChart().getXYPlot();
		
		@SuppressWarnings("unchecked")//TODO Sketchy AF
		List<XYGateAnnotation> annotations = plot.getAnnotations();
		
		Point2D p = ChartUtils.getPlotCoordinates(e, panel);
		
		List<XYGateAnnotation> result = annotations.stream()
				   								   .filter(annotation -> annotation instanceof XYGateAnnotation)
				                                   .filter(annotation -> annotation.containsPoint(p))
				                                   .collect(Collectors.toList());
		
		if(result.size()==1){
			if (result.get(0) instanceof RectangleGateAnnotation){
				RectangleGateAnnotation currentRect = (RectangleGateAnnotation) result.get(0);
				selectedAnnotation = result.get(0);
				RectangleGateAnnotation newAnnotation = new RectangleGateAnnotation(currentRect.getX0(), 
																			 currentRect.getY0(), 
																			 currentRect.getX1(),
																			 currentRect.getY1(),
																			 DefaultGraphics.SELECTED_STROKE, 
																			 DefaultGraphics.SELECTED_GATE_COLOR);
				newAnnotation.setToolTipText(currentRect.getToolTipText());
				plot.removeAnnotation(selectedAnnotation);
				plot.addAnnotation(newAnnotation);
			} else {
				
			}
		} else if (selectedAnnotation!=null){
			RectangleGateAnnotation oldAnnotation = (RectangleGateAnnotation) selectedAnnotation;
			RectangleGateAnnotation newAnnotation = new RectangleGateAnnotation(
					oldAnnotation.getX0(), 
					oldAnnotation.getY0(), 
					oldAnnotation.getX1(),
					oldAnnotation.getY1(),
					DefaultGraphics.DEFAULT_STROKE, 
					DefaultGraphics.DEFAULT_GATE_COLOR);
			newAnnotation.setToolTipText(oldAnnotation.getToolTipText());
			ChartUtils.updateRectangleAnnotation(oldAnnotation, newAnnotation, panel);
			plot.removeAnnotation(selectedAnnotation, true);
			plot.addAnnotation(newAnnotation);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e){
		
	}
}
