package io.landysh.inflor.main.core.gates.ui;

import java.awt.geom.Point2D;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.data.Range;

public interface XYGateAnnotation extends XYAnnotation {

  abstract String getSubsetName();

  abstract String getRangeAxisName();

  abstract String getDomainAxisName();

  void setSubsetName(String newName);

  void setRangeAxisName(String newName);

  void setDomainAxisName(String newName);

  /**
   * @return a boolean indicating if the point specified is within the area of the annotation.
   */
  public abstract boolean containsPoint(Point2D p);

  /**
   * @return a clone of the annotation, but with visual style reflecting that the annotation is
   *         selected.
   */
  public abstract XYGateAnnotation cloneSelected();

  /**
   * @return a clone of the annotation, but with the default (unselected) visual style
   */
  public abstract XYGateAnnotation cloneDefault();

  /**
   * creates a new XY Annotation moved by a distance of dx and xy
   * 
   * @param dx - horizontal distance to be moved.
   * @param dy - vertical distance to be moved
   * @return the new annotation.
   */
  public abstract XYGateAnnotation translate(double dx, double dy);

  public abstract void setToolTipText(String text);

  public abstract boolean matchesVertex(Point2D v, double xHandleSize, double yHandleSize);

  public abstract XYGateAnnotation updateVertex(Point2D v, double dx, double dy, double xHandleSize,
      double yHandleSize);

  abstract Range getXRange();// TODO rename

  abstract Range getYRange();// TODO rename



}
