package io.landysh.inflor.java.core.plots.gateui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.data.Range;

import io.landysh.inflor.java.core.ui.LookAndFeel;

@SuppressWarnings("serial")
public class RectangleGateAnnotation extends XYBoxAnnotation implements XYGateAnnotation{

	double x0;
	double y0;
	double x1;
	double y1;
	private String rangeAxisName;
	private String subsetName;
	private String domainAxisName;
	
	public RectangleGateAnnotation(String subsetName,String domainAxisName, String rangeAxisName,  
			double x0, double y0, double x1, double y1, BasicStroke stroke, Color color) {
		super(x0, y0, x1, y1, stroke, color);
		this.rangeAxisName = rangeAxisName;
		this.subsetName = subsetName;
		this.domainAxisName = domainAxisName;
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}
	
	public double getX0(){return x0;}
	public double getX1(){return x1;}
	public double getY0(){return y0;}
	public double getY1(){return y1;}

	@Override
	public boolean containsPoint(Point2D p) {
		if (x0<=p.getX()&&p.getX()<=x1&&y0<=p.getY()&&p.getY()<=y1){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public XYGateAnnotation cloneSelected() {
		return new RectangleGateAnnotation(subsetName, domainAxisName, rangeAxisName, x0, y0, x1, y1, 
				LookAndFeel.SELECTED_STROKE, 
				LookAndFeel.SELECTED_GATE_COLOR);
	}

	@Override
	public XYGateAnnotation cloneDefault() {
		return new RectangleGateAnnotation(subsetName, domainAxisName, rangeAxisName, x0, y0, x1, y1, 
				LookAndFeel.DEFAULT_STROKE, 
				LookAndFeel.DEFAULT_GATE_COLOR);
	}

	@Override
	public XYGateAnnotation translate(double dx, double dy) {
		return new RectangleGateAnnotation(subsetName, domainAxisName, rangeAxisName, x0+dx, y0+dy, x1+dx, y1+dy, 
				LookAndFeel.SELECTED_STROKE, 
				LookAndFeel.SELECTED_GATE_COLOR);
	}

	@Override
	public boolean matchesVertex(Point2D v, double xHandleSize, double yHandleSize) {
		double xMin = (v.getX()-xHandleSize);
		double xMax = (v.getX()+xHandleSize);
		double yMin = (v.getY()-yHandleSize);
		double yMax = (v.getY()+yHandleSize);
		boolean xMatches = false;
		boolean yMatches = false;
		if ((xMin<=x0&&x0<=xMax)||(xMin<=x1&&x1<=xMax)){xMatches = true;}
		if ((yMin<=y0&&y0<=yMax)||(yMin<=y1&&y1<=yMax)){yMatches = true;}
		if (xMatches&&yMatches) {return true;}
		return false;
	}

	@Override
	public XYGateAnnotation updateVertex(Point2D v, double dx, double dy, double xHandleSize, double yHandleSize) {
		double xMin = (v.getX()-xHandleSize);
		double xMax = (v.getX()+xHandleSize);
		double yMin = (v.getY()-yHandleSize);
		double yMax = (v.getY()+yHandleSize);

		if ((xMin<=x0&&x0<=xMax)){x0=x0+dx;}
		if ((xMin<=x1&&x1<=xMax)){x1=x1+dx;}
		if ((yMin<=y0&&y0<=yMax)){y0=y0+dy;}
		if ((yMin<=y1&&y1<=yMax)){y1=y1+dy;}

		return new RectangleGateAnnotation(subsetName, domainAxisName, rangeAxisName, x0, y0, x1, y1, 
				LookAndFeel.SELECTED_STROKE, 
				LookAndFeel.SELECTED_GATE_COLOR);
	}

	@Override
	public String getSubsetName() { return this.subsetName;}
	@Override
	public String getRangeAxisName() {return this.rangeAxisName;}
	@Override
	public String getDomainAxisName() {return this.domainAxisName;}
	@Override
	public void setSubsetName(String newName) {this.subsetName = newName;}
	@Override
	public void setRangeAxisName(String newName) {this.rangeAxisName = newName;}
	@Override
	public void setDomainAxisName(String newName) {this.domainAxisName = newName;}

	@Override
	public Range getXRange() {
		return new Range(Math.min(x0, x1), Math.max(x0, x1));
	}

	@Override
	public Range getYRange() {
		return new Range(Math.min(y0, y1), Math.max(y0, y1));
	}
}
