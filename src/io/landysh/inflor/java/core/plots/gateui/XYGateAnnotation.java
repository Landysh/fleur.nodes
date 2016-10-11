package io.landysh.inflor.java.core.plots.gateui;

import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYAnnotation;

public interface XYGateAnnotation extends XYAnnotation{
	
	/** 
	 * @return a boolean indicating if the point specified is within the area of the annotation.
	 */
	abstract boolean containsPoint(Point2D p);
	
	
	/** 
	 * @return a clone of the annotation, but with visual style reflecting that the annotation is selected.
	 */
	abstract XYGateAnnotation cloneSelected();
	
	/** 
	 * @return a clone of the annotation, but with the default (unselected) visual style
	 */
	abstract XYGateAnnotation cloneDefault();
	
	/**
	 * creates a new XY Annotation moved by a distance of dx and xy
	 * @param dx - horizontal distance to be moved.
	 * @param dy - vertical distance to be moved
	 * @return the new annotation.
	 */
	abstract XYGateAnnotation translate(double dx, double dy);

}
