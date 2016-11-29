package io.landysh.inflor.main.core.gates.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import io.landysh.inflor.main.core.plots.FCSChartPanel;
import io.landysh.inflor.main.core.ui.LookAndFeel;
import io.landysh.inflor.main.core.utils.ChartUtils;
import io.landysh.inflor.main.knime.nodes.createGates.ui.GateNameEditor;

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
    if (!(vert0 == null && vert1 == null)) {
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
      // Window topFrame = SwingUtilities.getWindowAncestor(panel);
      GateNameEditor dialog = new GateNameEditor();
      dialog.setVisible(true);
      // On Close...
      if (dialog.isOK) {
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
