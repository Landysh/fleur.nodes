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
package io.landysh.inflor.main.core.gates.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import io.landysh.inflor.main.core.plots.FCSChartPanel;
import io.landysh.inflor.main.core.ui.GateNameEditor;
import io.landysh.inflor.main.core.ui.LookAndFeel;
import io.landysh.inflor.main.core.utils.ChartUtils;

public class RectangleGateAdapter extends MouseInputAdapter {
  private FCSChartPanel panel;
  private Point2D vert1;
  private Point2D vert0;
  private double x0;
  private double x1;
  private double y0;
  private double y1;

  private XYGateAnnotation tempAnn;


  public RectangleGateAdapter(FCSChartPanel panel) {
    this.panel = panel;
  }

  private void updateVerticies() {
    if (vert0 != null && vert1 != null) {
      x0 = Math.min(vert0.getX(), vert1.getX());
      x1 = Math.max(vert0.getX(), vert1.getX());
      y0 = Math.min(vert0.getY(), vert1.getY());
      y1 = Math.max(vert0.getY(), vert1.getY());
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (tempAnn != null) {
      panel.removeTemporaryAnnotation(tempAnn);
    }
    if (SwingUtilities.isLeftMouseButton(e)) {
      vert1 = ChartUtils.getPlotCoordinates(e, panel);
      updateVerticies();
      tempAnn = new RectangleGateAnnotation(null, null, null, x0, y0, x1, y1,
          LookAndFeel.DEFAULT_STROKE, LookAndFeel.DEFAULT_GATE_COLOR);
      panel.addTemporaryAnnotation(tempAnn);
      // Pop a gate editor dialog
      GateNameEditor dialog = new GateNameEditor();
      dialog.setVisible(true);
      // On Close...
      if (dialog.isOK()) {
        XYGateAnnotation finalAnnotation = tempAnn.cloneDefault();
        finalAnnotation.setSubsetName(dialog.getGateName());
        finalAnnotation.setDomainAxisName(panel.getDomainAxisName());
        finalAnnotation.setRangeAxisName(panel.getRangeAxisName());
        panel.createGateAnnotation(finalAnnotation);
      }
      panel.removeTemporaryAnnotation(tempAnn);
      tempAnn = null;
      dialog.dispose();
      panel.activateGateSelectButton();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      vert0 = ChartUtils.getPlotCoordinates(e, panel);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      vert1 = ChartUtils.getPlotCoordinates(e, panel);
      updateVerticies();
      if (tempAnn != null) {
        panel.removeTemporaryAnnotation(tempAnn);
      }
      tempAnn = new RectangleGateAnnotation(null, null, null, x0, y0, x1, y1,
          LookAndFeel.DEFAULT_STROKE, LookAndFeel.DEFAULT_GATE_COLOR);
      panel.addTemporaryAnnotation(tempAnn);
    }
  }
}
