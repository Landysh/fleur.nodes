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

import java.util.UUID;

import org.jfree.chart.JFreeChart;

import fleur.core.data.FCSFrame;
import fleur.core.transforms.TransformSet;

public abstract class AbstractFCChart {

  /**
   * @Param newUUID creates a new UUID for this plot definition.
   */

  public final String uuid;
  protected ChartSpec spec;

  public AbstractFCChart(String priorUUID, ChartSpec spec) {
    // Create new UUID if needed.
    if (priorUUID == null) {
      uuid = UUID.randomUUID().toString();
    } else {
      uuid = priorUUID;
    }
    this.spec = spec;
  }

  public void setSpec(ChartSpec spec) {
    this.spec = spec;
  }

  public ChartSpec getSpec() {
    return this.spec;
  }

  public abstract JFreeChart createChart(FCSFrame dataFrame, TransformSet transforms);
}
