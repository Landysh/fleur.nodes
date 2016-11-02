package io.landysh.inflor.java.core.gates.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.data.Range;

import io.landysh.inflor.java.core.ui.LookAndFeel;

@SuppressWarnings("serial")
public class PolygonGateAnnotation extends XYPolygonAnnotation implements XYGateAnnotation {

  double[] polygonPoints;
  private String subsetName;
  private String rangeAxisName;
  private String domainAxisName;

  public PolygonGateAnnotation(String subsetName, String domainAxisName, String rangeAxisName,
      double[] polygon, BasicStroke stroke, Color color) {
    super(polygon, stroke, color);
    this.subsetName = subsetName;
    this.domainAxisName = domainAxisName;
    this.rangeAxisName = rangeAxisName;
    polygonPoints = polygon;
  }

  @Override
  public boolean containsPoint(Point2D p) {
    Path2D poly = new Path2D.Double();
    for (int i = 0; i < polygonPoints.length; i += 2) {
      if (i == 0) {
        poly.moveTo(polygonPoints[i], polygonPoints[i + 1]);
      } else {
        poly.lineTo(polygonPoints[i], polygonPoints[i + 1]);
      }
    }
    poly.closePath();
    return poly.contains(p);
  }

  @Override
  public XYGateAnnotation cloneSelected() {
    return new PolygonGateAnnotation(subsetName, domainAxisName, rangeAxisName, polygonPoints,
        LookAndFeel.SELECTED_STROKE, LookAndFeel.SELECTED_GATE_COLOR);
  }

  @Override
  public XYGateAnnotation cloneDefault() {
    return new PolygonGateAnnotation(subsetName, domainAxisName, rangeAxisName, polygonPoints,
        LookAndFeel.DEFAULT_STROKE, LookAndFeel.DEFAULT_GATE_COLOR);
  }

  @Override
  public XYGateAnnotation translate(double dx, double dy) {
    double[] translatedPoints = new double[polygonPoints.length];
    for (int i = 0; i < translatedPoints.length; i += 2) {
      translatedPoints[i] = polygonPoints[i] + dx;
      translatedPoints[i + 1] = polygonPoints[i + 1] + dy;
    }
    return new PolygonGateAnnotation(subsetName, domainAxisName, rangeAxisName, translatedPoints,
        LookAndFeel.SELECTED_STROKE, LookAndFeel.SELECTED_GATE_COLOR);
  }

  public double[] getDomainPoints() {
    double[] domainPoints = new double[polygonPoints.length / 2];
    for (int i = 0; i < polygonPoints.length; i++) {
      domainPoints[i / 2] = polygonPoints[i];
      i++;
    }
    return domainPoints;
  }

  public double[] getRangePoints() {
    double[] rangePoints = new double[polygonPoints.length / 2];
    for (int i = 0; i < polygonPoints.length; i++) {
      rangePoints[i / 2] = polygonPoints[i + 1];
      i++;
    }
    return rangePoints;
  }

  @Override
  public boolean matchesVertex(Point2D v, double xHandleSize, double yHandleSize) {
    double[] x = getDomainPoints();
    double[] y = getRangePoints();
    double xMin = (v.getX() - xHandleSize);
    double xMax = (v.getX() + xHandleSize);
    double yMin = (v.getY() - yHandleSize);
    double yMax = (v.getY() + yHandleSize);
    for (int i = 0; i < x.length; i++) {
      if (xMin <= x[i] && x[i] <= xMax) {
        if (yMin <= y[i] && y[i] <= yMax) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public XYGateAnnotation updateVertex(Point2D v, double dx, double dy, double xHandleSize,
      double yHandleSize) {
    double[] x = getDomainPoints();
    double[] y = getRangePoints();
    double xMin = (v.getX() - xHandleSize);
    double xMax = (v.getX() + xHandleSize);
    double yMin = (v.getY() - yHandleSize);
    double yMax = (v.getY() + yHandleSize);
    for (int i = 0; i < x.length; i++) {
      if (xMin <= x[i] && x[i] <= xMax) {
        if (yMin <= y[i] && y[i] <= yMax) {
          x[i] = x[i] + dx;
          y[i] = y[i] + dy;
        }
      }
    }

    double[] newPoints = new double[polygonPoints.length];
    for (int i = 0; i < newPoints.length; i++) {
      newPoints[i] = x[i / 2];
      newPoints[i + 1] = y[i / 2];
      i++;
    }

    return new PolygonGateAnnotation(subsetName, domainAxisName, rangeAxisName, newPoints,
        LookAndFeel.SELECTED_STROKE, LookAndFeel.SELECTED_GATE_COLOR);
  }

  @Override
  public String getSubsetName() {
    return this.subsetName;
  }

  @Override
  public String getRangeAxisName() {
    return this.rangeAxisName;
  }

  @Override
  public String getDomainAxisName() {
    return this.domainAxisName;
  }

  @Override
  public void setSubsetName(String newName) {
    this.subsetName = newName;
  }

  @Override
  public void setRangeAxisName(String newName) {
    this.rangeAxisName = newName;
  }

  @Override
  public void setDomainAxisName(String newName) {
    this.domainAxisName = newName;
  }

  @Override
  public Range getXRange() {
    double[] x = getDomainPoints();
    Arrays.sort(x);
    return new Range(x[0], x[x.length - 1]);
  }

  @Override
  public Range getYRange() {
    double[] y = getRangePoints();
    Arrays.sort(y);
    return new Range(y[0], y[y.length - 1]);
  }
}
