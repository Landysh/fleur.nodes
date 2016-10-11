package io.landysh.inflor.java.core.plots.gateui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYBoxAnnotation;

@SuppressWarnings("serial")
public class RectangleGateAnnotation extends XYBoxAnnotation implements XYGateAnnotation{

	double x0;
	double y0;
	double x1;
	double y1;
	
	public RectangleGateAnnotation(double x0, double y0, double x1, double y1, BasicStroke selectedStroke, Color defaultGateColor) {
		super(x0, y0, x1, y1, selectedStroke, defaultGateColor);
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}
	
	double getX0(){return x0;}
	double getX1(){return x1;}
	double getY0(){return y0;}
	double getY1(){return y1;}

	@Override
	public boolean containsPoint(Point2D p) {
		if (x0<=p.getX()&&p.getX()<=x1&&y0<=p.getY()&&p.getY()<=y1){
			return true;
		} else {
			return false;
		}
	}
}
