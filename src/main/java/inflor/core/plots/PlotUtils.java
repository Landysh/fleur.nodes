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
package main.java.inflor.core.plots;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.Range;

import main.java.inflor.core.fcs.ParameterTypes;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.transforms.BoundDisplayTransform;
import main.java.inflor.core.transforms.LogicleTransform;
import main.java.inflor.core.transforms.LogrithmicTransform;
import main.java.inflor.core.transforms.TransformType;

public class PlotUtils {
  
  private PlotUtils(){}
  
  public static ValueAxis createAxis(String name, AbstractTransform transform) {
    if (transform instanceof BoundDisplayTransform) {
      NumberAxis axis = new NumberAxis(name);
      BoundDisplayTransform bdt = (BoundDisplayTransform) transform;
      axis.setRange(new Range(bdt.getMinTranformedValue(), bdt.getMaxValue()));
      return axis;
    } else if (transform instanceof LogicleTransform) {
      LogicleTransform llt = (LogicleTransform) transform;
      return new LogicleNumberAxis(name, llt);
    } else if (transform instanceof LogrithmicTransform) {
      NumberAxis axis = new NumberAxis(name);
      LogrithmicTransform logTransform = (LogrithmicTransform) transform;
      axis.setRange(new Range(logTransform.getMin(), logTransform.getMax()));
      return axis;
    } else {
      throw new IllegalArgumentException("Transformation type not supported. Yet.");
    }
  }

  public static AbstractFCChart createPlot(ChartSpec plotSpec) {
    PlotTypes type = plotSpec.getPlotType();
    AbstractFCChart newPlot = null;
    if (type.equals(PlotTypes.DENSITY)) {
      newPlot = new DensityPlot(plotSpec);
    } else if (type.equals(PlotTypes.HISTOGRAM)) {
      newPlot = new HistogramPlot(plotSpec);
    } else {
      //noop
    }
    return newPlot;
  }

  public static AbstractTransform createDefaultTransform(TransformType selectedType) {

    AbstractTransform newTransform = null;

    if (selectedType == TransformType.LINEAR
        ||selectedType == TransformType.BOUNDARY) {
      newTransform = new BoundDisplayTransform(0, 262144);
    } else if (selectedType == TransformType.LOGARITHMIC) {
      newTransform = new LogrithmicTransform(100, 262144);
    } else if (selectedType == TransformType.LOGICLE) {
      newTransform = new LogicleTransform();
    } else {
      //noop
    }
    return newTransform;
  }

  public static LookupPaintScale createPaintScale(double zMax,ColorSchemes colorScheme) {
    PaintModel pm = new PaintModel(colorScheme, zMax);
    Paint[] paints = pm.getPaints();
    double[] levels = pm.getLevels();
    LookupPaintScale paintScale = new LookupPaintScale(0, pm.getThreshold(), Color.GRAY);
    for (int i = 0; i < levels.length; i++) {
      paintScale.add(levels[i], paints[i]);
    }
    return paintScale;
  }

  public static AbstractTransform createDefaultTransform(String parameterName) {
    if (ParameterTypes.DNA.matches(parameterName) || ParameterTypes.FORWARD_SCATTER.matches(parameterName)
        || ParameterTypes.SIDE_SCATTER.matches(parameterName)|| ParameterTypes.TIME.matches(parameterName)) {
      return new BoundDisplayTransform();
    } else {
      return new LogicleTransform();
    }
  }
}
