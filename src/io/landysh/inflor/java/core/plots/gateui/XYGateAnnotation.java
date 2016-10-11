package io.landysh.inflor.java.core.plots.gateui;

import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYAnnotation;

public interface XYGateAnnotation extends XYAnnotation{

	abstract boolean containsPoint(Point2D p);

}
