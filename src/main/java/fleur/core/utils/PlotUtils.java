/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package fleur.core.utils;

import java.awt.Color;
import java.awt.Paint;
import java.util.logging.Logger;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.Range;

import fleur.core.fcs.DimensionTypes;
import fleur.core.logging.LogFactory;
import fleur.core.plots.AbstractFCChart;
import fleur.core.plots.ChartSpec;
import fleur.core.plots.ColorSchemes;
import fleur.core.plots.DensityPlot;
import fleur.core.plots.HistogramPlot;
import fleur.core.plots.LogicleNumberAxis;
import fleur.core.plots.PaintModel;
import fleur.core.plots.PlotTypes;
import fleur.core.plots.ScatterPlot;
import fleur.core.transforms.AbstractTransform;
import fleur.core.transforms.BoundDisplayTransform;
import fleur.core.transforms.LogicleTransform;
import fleur.core.transforms.LogrithmicTransform;
import fleur.core.transforms.TransformType;

public class PlotUtils {

  private PlotUtils() {}

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
    if (type.equals(PlotTypes.SCATTER)) {
      newPlot = new ScatterPlot(plotSpec);
    } else if (type.equals(PlotTypes.HISTOGRAM)) {
      newPlot = new HistogramPlot(plotSpec);
    } else if (type.equals(PlotTypes.DENSITY)) {
      newPlot = new DensityPlot(plotSpec);
    } else {
      Logger logger = LogFactory.createLogger(PlotUtils.class.getName());
      logger.fine("PlotType not supported: " + type.name());
    }
    return newPlot;
  }

  public static AbstractTransform createDefaultTransform(TransformType selectedType, Double range) {

    AbstractTransform newTransform = null;

    if (selectedType == TransformType.LINEAR || selectedType == TransformType.BOUNDARY) {
      newTransform = new BoundDisplayTransform(Double.MAX_VALUE, Double.MAX_VALUE);
    } else if (selectedType == TransformType.LOGARITHMIC) {
      newTransform = new LogrithmicTransform(1, 1000000);
    } else if (selectedType == TransformType.LOGICLE && null!=range) {
      newTransform = new LogicleTransform(range);
    } else {
      // noop
    }
    return newTransform;
  }

  public static LookupPaintScale createPaintScale(double zMax, ColorSchemes colorScheme) {
    PaintModel pm = new PaintModel(colorScheme, zMax);
    Paint[] paints = pm.getPaints();
    double[] levels = pm.getLevels();
    LookupPaintScale paintScale = new LookupPaintScale(0, pm.getThreshold(), Color.GRAY);
    for (int i = 0; i < levels.length; i++) {
      paintScale.add(levels[i], paints[i]);
    }
    return paintScale;
  }
  
  
  // TODO: move to FCSUtils?
  public static AbstractTransform createDefaultTransform(String parameterName, Double range) {
    if (DimensionTypes.DNA.matches(parameterName)
        || DimensionTypes.FORWARD_SCATTER.matches(parameterName)
        || DimensionTypes.SIDE_SCATTER.matches(parameterName)
        || DimensionTypes.TIME.matches(parameterName)
        || FCSUtilities.MERGE_DIMENSION_NAME.equals(parameterName)){
      return new BoundDisplayTransform(0, 262144);
      
    } else if (DimensionTypes.PULSE_WIDTH.matches(parameterName)) {
       return new BoundDisplayTransform(0, 100);
    } else {
      return new LogicleTransform(range);
    }
  }
}
