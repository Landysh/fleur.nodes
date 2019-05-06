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
package fleur.core.plots;

import java.awt.Color;
import java.awt.Paint;

public class PaintModel {

  double[] discreteData;
  double[] levelValues;
  Paint[] paint;
  private int levels;
  private ColorSchemes colorScheme;

  public PaintModel(ColorSchemes colorScheme, double zMax) {

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

    if (colorScheme == ColorSchemes.COLOR) {
      float startH = 200 / 360f;
      float deltaH = startH / levels;

      for (int i = 0; i < colorScale.length; i++) {
        float hue = startH - (i) * deltaH;
        colorScale[i] = Color.getHSBColor(hue, 0.7f, 1f);
      }
    } else if (colorScheme == ColorSchemes.GRAYSCALE) {
      float startV = 200 / 360f;
      float deltaV = startV / levels;

      for (int i = 0; i < colorScale.length; i++) {
        float v = startV - (i) * deltaV;
        colorScale[i] = Color.getHSBColor(0f, 0f, v);
      }
    } else {
      // fallback, should not be used.
      for (int i = 0; i < colorScale.length; i++) {
        colorScale[i] = Color.getHSBColor(i / 360f, i / 360f, i / 100f);
      }
    }

    return colorScale;
  }

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