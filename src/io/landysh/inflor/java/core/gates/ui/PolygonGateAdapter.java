package io.landysh.inflor.java.core.gates.ui;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jfree.chart.annotations.XYLineAnnotation;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.ui.LookAndFeel;
import io.landysh.inflor.java.core.utils.ChartUtils;
import io.landysh.inflor.java.knime.nodes.createGates.ui.GateNameEditor;

public class PolygonGateAdapter extends MouseInputAdapter {
  private FCSChartPanel panel;
  private ArrayList<Point2D> vertices = new ArrayList<>();
  private ArrayList<XYLineAnnotation> segments;
  private Point2D anchorPoint;
  private XYLineAnnotation anchorSegment;

  public PolygonGateAdapter(FCSChartPanel panel) {
    this.panel = panel;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Point2D v = ChartUtils.getPlotCoordinates(e, panel);
    if (SwingUtilities.isLeftMouseButton(e)) {
      // add the next segment
      anchorPoint = v;
      vertices.add(v);
      updateTemporaryAnnotation();
    }
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
      XYLineAnnotation closingSegment = new XYLineAnnotation(anchorPoint.getX(), anchorPoint.getY(),
          vertices.get(0).getX(), vertices.get(0).getY());
      segments.add(closingSegment);
      panel.addTemporaryAnnotation(closingSegment);

      // Finish the polygon and ask for a name
      int pointCount = vertices.size() * 2;
      double[] polygon = new double[pointCount];
      for (int i = 0; i < pointCount; i++) {
        polygon[i] = vertices.get(i / 2).getX();
        polygon[i + 1] = vertices.get(i / 2).getY();
        i++;
      }
      // Pop a gate editor dialog
      // Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(panel);
      GateNameEditor dialog = new GateNameEditor();
      dialog.setVisible(true);
      // On Close...
      if (dialog.isOK) {
        PolygonGateAnnotation finalPolygon = new PolygonGateAnnotation(dialog.getGateName(),
            panel.getDomainAxisName(), panel.getRangeAxisName(), polygon,
            LookAndFeel.DEFAULT_STROKE, LookAndFeel.DEFAULT_GATE_COLOR);
        panel.createGateAnnotation(finalPolygon);
      }
      dialog.dispose();

      // remove the anchor point && cleanup segments.
      vertices.clear();
      panel.removeTemporaryAnnotation(anchorSegment);
      anchorSegment = null;
      anchorPoint = null;
      if (segments != null) {
        segments.forEach(annotation -> panel.removeTemporaryAnnotation(annotation));
      }
      segments = null;
      panel.activateGateSelectButton();
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (anchorSegment != null) {
      panel.removeTemporaryAnnotation(anchorSegment);
    }
    if (anchorPoint != null) {
      Point2D p = ChartUtils.getPlotCoordinates(e, panel);
      anchorSegment =
          new XYLineAnnotation(anchorPoint.getX(), anchorPoint.getY(), p.getX(), p.getY());
      panel.addTemporaryAnnotation(anchorSegment);
    }
  }

  private void updateTemporaryAnnotation() {
    Point2D previousVertex = null;
    if (segments != null) {
      segments.stream().forEach(segment -> panel.removeTemporaryAnnotation(segment));
    }
    segments = new ArrayList<XYLineAnnotation>();
    for (Point2D v : vertices) {
      if (previousVertex == null) {
        previousVertex = v;
      } else {
        segments.add(
            new XYLineAnnotation(previousVertex.getX(), previousVertex.getY(), v.getX(), v.getY()));
        previousVertex = v;
      }
    }
    segments.stream().forEach(segment -> panel.addTemporaryAnnotation(segment));
  }
}
