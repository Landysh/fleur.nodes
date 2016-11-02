package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;

public class PaintModel {

  double[] discreteData;
  double[] levelValues;
  Paint[] paint;
  private int levels;
  // private double zMin;
  // private double zMax;
  private ColorSchemes colorScheme;

  public PaintModel(ColorSchemes colorScheme, double zMin, double zMax) {

    // this.zMin = zMin;
    // this.zMax = zMax;
    this.colorScheme = colorScheme;

    levels = (int) zMax;
    paint = createPaintArray(levels);
    levelValues = new double[levels];
    for (int i = 0; i < levels; i++) {
      levelValues[i] = i;
    }
  }

  private Paint[] createPaintArray(int levels) {
    Paint[] colorScale = new Paint[levels];

    if (colorScheme == ColorSchemes.COOL_HEATMAP) {
      float startH = 200 / 360f;
      float deltaH = startH / levels;

      for (int i = 0; i < colorScale.length; i++) {
        float hue = startH - (i) * deltaH;
        colorScale[i] = Color.getHSBColor(hue, 0.7f, 1f);
      }
    } else if (colorScheme == ColorSchemes.GRAY_SCALE) {
      float startV = 200 / 360f;
      float deltaV = startV / levels;

      for (int i = 0; i < colorScale.length; i++) {
        float v = startV - (i) * deltaV;
        colorScale[i] = Color.getHSBColor(0f, 0f, v);
      }
    } else {
      // fallback, should not be used.
      for (int i = 0; i < colorScale.length; i++) {
        colorScale[i] = Color.getHSBColor(i / 360f, i / 360f, i / 100);
      }
    }

    return colorScale;
  }

  // public double[] getDiscreteData(double[] z) {
  // return discretizeData(z, levels);
  // }

  public Paint[] getPaints() {
    return paint;
  }

  public double[] getLevels() {
    return levelValues;
  }

  public double getThreshold() {
    return levels;
  }

}
