package io.landysh.inflor.java.core.gates.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.utils.ChartUtils;

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
