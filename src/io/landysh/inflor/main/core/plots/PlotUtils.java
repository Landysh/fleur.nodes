package io.landysh.inflor.main.core.plots;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.Range;

import io.landysh.inflor.main.core.fcs.ParameterTypes;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.main.core.transforms.LogicleTransform;
import io.landysh.inflor.main.core.transforms.LogrithmicTransform;
import io.landysh.inflor.main.core.transforms.TransformType;

public class PlotUtils {

  public static ValueAxis createAxis(String Name, AbstractTransform transform) {
    if (transform instanceof BoundDisplayTransform) {
      NumberAxis axis = new NumberAxis(Name);
      BoundDisplayTransform bdt = (BoundDisplayTransform) transform;
      axis.setRange(new Range(bdt.getMinTranformedValue(), bdt.getMaxValue()));
      return axis;
    } else if (transform instanceof LogicleTransform) {
      LogicleTransform llt = (LogicleTransform) transform;
      LogicleNumberAxis axis = new LogicleNumberAxis(Name, llt);
      return axis;
    } else if (transform instanceof LogrithmicTransform) {
      NumberAxis axis = new NumberAxis(Name);
      LogrithmicTransform logTransform = (LogrithmicTransform) transform;
      axis.setRange(new Range(logTransform.getMin(), logTransform.getMax()));
      return axis;
    } else {
      throw new RuntimeException("Transformation type not supported. Yet.");
    }
  }

  public static AbstractFCChart createPlot(ChartSpec plotSpec) {
    PlotTypes type = plotSpec.getPlotType();
    AbstractFCChart newPlot = null;
    if (type.equals(PlotTypes.Density)) {
      newPlot = new DensityPlot(plotSpec);
    } else if (type.equals(PlotTypes.Histogram)) {
      newPlot = new HistogramPlot(plotSpec);
    } else {
      throw new RuntimeException("No valid plot type selected.");
    }
    return newPlot;
  }

  public static AbstractTransform createDefaultTransform(TransformType selectedType) {

    AbstractTransform newTransform;

    if (selectedType == TransformType.LINEAR) {
      newTransform = new BoundDisplayTransform(0, 262144);
    } else if (selectedType == TransformType.BOUNDARY) {
      newTransform = new BoundDisplayTransform(0, 262144);
    } else if (selectedType == TransformType.LOGARITHMIC) {
      newTransform = new LogrithmicTransform(100, 262144);
    } else if (selectedType == TransformType.LOGICLE) {
      newTransform = new LogicleTransform();
    } else {
      throw new RuntimeException("Invslid transform type selected.");
    }
    return newTransform;
  }

  public static LookupPaintScale createPaintScale(double zMin, double zMax,
      ColorSchemes colorScheme) {
    PaintModel pm = new PaintModel(colorScheme, zMin, zMax);
    Paint[] paints = pm.getPaints();
    double[] levels = pm.getLevels();
    LookupPaintScale paintScale = new LookupPaintScale(0, pm.getThreshold(), Color.red);
    for (int i = 0; i < levels.length; i++) {
      paintScale.add(levels[i], paints[i]);
    }
    return paintScale;
  }

  public static AbstractTransform createDefaultTransform(String parameterName) {
    if (ParameterTypes.DNA.matches(parameterName) || ParameterTypes.ForwardScatter.matches(parameterName)
        || ParameterTypes.SideScatter.matches(parameterName)|| ParameterTypes.TIME.matches(parameterName)) {
      return new BoundDisplayTransform();
    } else {
      return new LogicleTransform();
    }
  }
}
