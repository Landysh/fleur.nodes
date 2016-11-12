package io.landysh.inflor.java.core.gates;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.BitSet;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.utils.FCSUtilities;

public class PolygonGate extends AbstractGate {

  /**
   * 
   */
  private static final long serialVersionUID = 7227537695328250955L;

  String gateLabel;
  String domainName;
  String rangeName;
  ArrayList<Double> domainPoints;
  ArrayList<Double> rangePoints;

  public PolygonGate(String label, String domainName, double[] domainPoints, String rangeName,
      double[] rangePoints, String priorUUID) {
    super(priorUUID);
    this.gateLabel = label;
    this.domainName = domainName;
    this.rangeName = rangeName;
    this.domainPoints = new ArrayList<Double>();
    for (double d : domainPoints) {
      this.domainPoints.add(d);
    }
    this.rangePoints = new ArrayList<Double>();
    for (double d : rangePoints) {
      this.rangePoints.add(d);
    }
  }

  public PolygonGate(String label, String domainName, double[] domainPoints, String rangeName,
      double[] rangePoints) {
    this(label, domainName, domainPoints, rangeName, rangePoints, null);
  }

  @Override
  public BitSet evaluate(FCSFrame data) {
    double[] d1Data = FCSUtilities.findCompatibleDimension(data, domainName).getData();
    double[] d2Data = FCSUtilities.findCompatibleDimension(data, rangeName).getData();
    Path2D poly = new Path2D.Double();
    BitSet mask = new BitSet(data.getRowCount());
    for (int i = 0; i < domainPoints.size(); i++) {
      if (i == 0) {
        poly.moveTo(domainPoints.get(i), rangePoints.get(i));
      } else {
        poly.lineTo(domainPoints.get(i), rangePoints.get(i));
      }
    }
    poly.closePath();

    for (int i = 0; i < d1Data.length; i++) {
      if (poly.contains(d1Data[i], d2Data[i])) {
        mask.set(i);
      }
    }
    return mask;
  }

  public String[] getDimensionNames() {
    return new String[] {domainName, rangeName};
  }


  @Override
  public Element toXMLElement() {
    // TODO
    return null;
  }

  public void updatePoint(int index, double d1New, double d2New) {
    domainPoints.set(index, d1New);
    rangePoints.set(index, d2New);
  }

  @Override
  public void validate() throws IllegalStateException {
    if (domainPoints.size() < 3 || rangePoints.size() < 3) {
      final String message = "A polygon requires at least 3 verticies!";
      final IllegalStateException ise = new IllegalStateException(message);
      ise.printStackTrace();
      throw ise;
    }

    if (domainPoints.size() != rangePoints.size()) {
      final String message = "A polygon requires the same number of points in both dimensions.";
      final IllegalStateException ise = new IllegalStateException(message);
      ise.printStackTrace();
      throw ise;
    }
  }

  public double[] getVertex(int i) {
    return new double[] {domainPoints.get(i), rangePoints.get(i)};
  }

  @Override
  public String getDomainAxisName() {
    return domainName;
  }

  @Override
  public String getRangeAxisName() {
    return rangeName;
  }

  public String getLabel() {
    return this.gateLabel;
  }

  public double[] getFlatVertexArray() {
    double[] result = new double[2*domainPoints.size()];
    for(int i=0;i<result.length;i++){
      result[i] = domainPoints.get(i/2);
      result[i+1] = rangePoints.get(i/2);
    }
    return result;
  }
}
