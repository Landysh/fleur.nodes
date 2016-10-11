package io.landysh.inflor.java.core.plots.gateui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYPolygonAnnotation;

import io.landysh.inflor.java.core.ui.LookAndFeel;

@SuppressWarnings("serial")
public class PolygonGateAnnotation extends XYPolygonAnnotation implements XYGateAnnotation{

	double[] polygonPoints;
	
	public PolygonGateAnnotation(double[] polygon) {
		super(polygon);
		polygonPoints = polygon;
	}

	public PolygonGateAnnotation(double[] polygon, BasicStroke stroke, Color color) {
		super(polygon, stroke, color);
	}

	@Override
	public boolean containsPoint(Point2D p) {
		Path2D poly = new Path2D.Double();
		for (int i=0;i<polygonPoints.length;i+=2){
			poly.moveTo(polygonPoints[i],polygonPoints[i+1]);
		}
		poly.moveTo(polygonPoints[0], polygonPoints[1]);
		poly.closePath();
		if (poly.contains(p)){
			return true;
		} else{
			return false;
		}
		
	}

	@Override
	public XYGateAnnotation cloneSelected() {
		return new PolygonGateAnnotation(
				polygonPoints, 
				LookAndFeel.SELECTED_STROKE, 
				LookAndFeel.SELECTED_GATE_COLOR);
	}

	@Override
	public XYGateAnnotation cloneDefault() {
		return new PolygonGateAnnotation(
				polygonPoints, 
				LookAndFeel.DEFAULT_STROKE, 
				LookAndFeel.DEFAULT_GATE_COLOR);
	}

	@Override
	public XYGateAnnotation translate(double dx, double dy) {
		double[] translatedPoints = new double[polygonPoints.length];
		for (int i=0;i<translatedPoints.length;i+=2){
			translatedPoints[i] = polygonPoints[i] + dx;
			translatedPoints[i+1] = polygonPoints[i+1] + dy;
		}
		return new PolygonGateAnnotation(
				translatedPoints, 
				LookAndFeel.SELECTED_STROKE, 
				LookAndFeel.SELECTED_GATE_COLOR);
	}
}
