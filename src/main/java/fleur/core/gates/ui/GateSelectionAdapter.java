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
package fleur.core.gates.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import fleur.core.plots.FCSChartPanel;
import fleur.core.utils.ChartUtils;

public class GateSelectionAdapter extends MouseAdapter {

  private FCSChartPanel panel;
  private Point2D v0;
  MouseEvent mousePressedEvent = null;

  public GateSelectionAdapter(FCSChartPanel panel) {
    this.panel = panel;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    mousePressedEvent = e;
    v0 = ChartUtils.getPlotCoordinates(e, panel);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      Point2D p = ChartUtils.getPlotCoordinates(e, panel);
      panel.setSelectAnnotations(p);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    mousePressedEvent = null;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    Point2D v = ChartUtils.getPlotCoordinates(e, panel);
    double dx = (v.getX() - v0.getX());
    double dy = (v.getY() - v0.getY());
    if (SwingUtilities.isLeftMouseButton(mousePressedEvent) && panel.hasGatesAtPoint(v0)) {
      panel.adjustGatesAtPoint(v0, dx, dy);
    } else if (SwingUtilities.isLeftMouseButton(mousePressedEvent)) {
      panel.translateSelectedAnnotations(dx, dy);
    }
    v0 = v;
  }
}
