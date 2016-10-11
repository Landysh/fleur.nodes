package io.landysh.inflor.java.core.plots.gateui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.utils.ChartUtils;

public class GateSelectionAdapter extends MouseAdapter {
	
	//private XYGateAnnotation currentXYAnn;
	private FCSChartPanel panel;
	private List<XYGateAnnotation> selectedAnnotations;
	private Point2D v0;

	public GateSelectionAdapter(FCSChartPanel panel) {
		this.panel = panel;
	}

	public void selectAnnotations(MouseEvent e){		
		@SuppressWarnings("unchecked")
		List<XYGateAnnotation> annotations = panel.getChart().getXYPlot().getAnnotations();
		Point2D p = ChartUtils.getPlotCoordinates(e, panel);
		//Sort annotations into selected and unselected lists
		Map<Boolean, List<XYGateAnnotation>> gateSelection = annotations
				   .stream()
				   .filter(annotation -> annotation instanceof XYGateAnnotation)
				   .collect(Collectors.partitioningBy(annotation -> annotation.containsPoint(p)));
		
		selectedAnnotations = gateSelection
			.get(new Boolean(true))
			.stream()
			.map(annotation -> updateSelectionStatus(panel, annotation, true))
			.collect(Collectors.toList());
		
		gateSelection
				.get(new Boolean(false))
				.stream()
				.forEach(annotation -> updateSelectionStatus(panel, annotation, false));
	}

	private XYGateAnnotation updateSelectionStatus(FCSChartPanel panel, XYGateAnnotation priorAnnotation, boolean markSelected) {
		XYGateAnnotation udpatedAnnotation;
		if (markSelected){
			udpatedAnnotation = priorAnnotation.cloneSelected();
		} else {
			udpatedAnnotation = priorAnnotation.cloneDefault();
		}
		ChartUtils.updateXYAnnotation(priorAnnotation, udpatedAnnotation, panel);
		return udpatedAnnotation;
	}
	
	private XYGateAnnotation moveAnnotation(FCSChartPanel panel, XYGateAnnotation priorAnnotation, double dx, double dy) {
		XYGateAnnotation udpatedAnnotation;
			udpatedAnnotation = priorAnnotation.translate(dx, dy);
		ChartUtils.updateXYAnnotation(priorAnnotation, udpatedAnnotation, panel);
		return udpatedAnnotation;
	}

	@Override
	public void mousePressed(MouseEvent e){
		v0 = ChartUtils.getPlotCoordinates(e, panel);
		selectAnnotations(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e){
		Point2D v = ChartUtils.getPlotCoordinates(e, panel);
		double dx = (v.getX() - v0.getX());
		double dy = (v.getY() - v0.getY());
		v0=v;
		if (selectedAnnotations!=null&&selectedAnnotations.size()>=1){
			List<XYGateAnnotation> translatedAnnoations = selectedAnnotations
				.stream()
				.map(annotation -> moveAnnotation(panel, annotation, dx, dy))
				.collect(Collectors.toList());
			selectedAnnotations = translatedAnnoations;
		}
	}
}
