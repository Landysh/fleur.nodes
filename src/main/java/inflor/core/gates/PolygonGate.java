/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.gates;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Optional;

import org.w3c.dom.Element;

import fleur.core.data.FCSDimension;
import fleur.core.data.FCSFrame;
import fleur.core.transforms.TransformSet;
import inflor.core.proto.FCSFrameProto;
import inflor.core.proto.FCSFrameProto.Message.Subset.Type;
import inflor.core.utils.FCSUtilities;

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
    this.domainPoints = new ArrayList<>();
    for (double d : domainPoints) {
      this.domainPoints.add(d);
    }
    this.rangePoints = new ArrayList<>();
    for (double d : rangePoints) {
      this.rangePoints.add(d);
    }
  }

  public PolygonGate(String label, String domainName, double[] domainPoints, String rangeName,
      double[] rangePoints) {
    this(label, domainName, domainPoints, rangeName, rangePoints, null);
  }

  @Override
  public BitSet evaluate(FCSFrame data, TransformSet transforms) {
    Optional<FCSDimension> d1 = FCSUtilities.findCompatibleDimension(data, domainName);
    Optional<FCSDimension> d2 = FCSUtilities.findCompatibleDimension(data, rangeName);
    double[] d1Data = transforms.get(d1.get().getShortName()).transform(d1.get().getData());
    double[] d2Data = transforms.get(d2.get().getShortName()).transform(d2.get().getData());
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
  
  @Override
  public String getLabel() {
    return this.gateLabel;
  }

  public double[] getFlatVertexArray() {
    double[] result = new double[2*domainPoints.size()];
    for(int i=0;i<result.length;i+=2){
      result[i] = domainPoints.get(i/2);
      result[i+1] = rangePoints.get(i/2);
    }
    return result;
  }

  @Override
  public Type getType() {
    return FCSFrameProto.Message.Subset.Type.POLYGON;
  }

  @Override
  public String[] getDimensions() {
    return new String[] {domainName, rangeName};
  }

  @Override
  public Double[] getDescriptors() {
    /**
     * Only recasting to big D double. 
     * Proto likes have big D, JFree little d. 
     */
    double[] flatVertices = getFlatVertexArray();
    Double[] descriptors = new Double[getFlatVertexArray().length];
    for (int i=0;i<flatVertices.length;i++) 
      descriptors[i] = flatVertices[i];
    return descriptors;
  }
  
  @Override
  public String toString(){
    return String.join(" ", new String[]{"Polygon Gate: ", getLabel(), getDomainAxisName(), getRangeAxisName()});
  }
  
}
