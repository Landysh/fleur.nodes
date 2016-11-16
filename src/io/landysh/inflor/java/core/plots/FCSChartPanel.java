package io.landysh.inflor.java.core.plots;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.gates.ui.SelectionButtonListener;
import io.landysh.inflor.java.core.gates.ui.XYGateAnnotation;
import io.landysh.inflor.java.core.utils.BitSetUtils;
import io.landysh.inflor.java.core.utils.ChartUtils;

@SuppressWarnings("serial")
public class FCSChartPanel extends ChartPanel {
  /**
   * Extends the JFreeChart ChartPanel to provide ability to persists annotations and map them to
   * gates.
   */

  private static final String DELETE_ANNOTATIONS_KEY = "delete selected annotations";

  private FCSFrame data;
  private List<XYGateAnnotation> selectedAnnotations = new ArrayList<XYGateAnnotation>();
  private HashMap<XYGateAnnotation, XYTextAnnotation> gateAnnotations = new HashMap<>();
  private double xHandleSize;
  private double yHandleSize;

  private JButton selectionButton;
  private ChartSpec spec;

  public FCSChartPanel(JFreeChart chart, ChartSpec spec, FCSFrame data) {
    super(chart);
    this.data = data;
    this.spec = spec;

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"),
        DELETE_ANNOTATIONS_KEY);
    getActionMap().put(DELETE_ANNOTATIONS_KEY, new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent ae) {
        deleteSelectedAnnotations();
      }
    });
    Range xRange = this.getChart().getXYPlot().getDomainAxis().getRange();
    xHandleSize = (xRange.getUpperBound() - xRange.getLowerBound()) / 100;
    Range yRange = this.getChart().getXYPlot().getDomainAxis().getRange();
    yHandleSize = (yRange.getUpperBound() - yRange.getLowerBound()) / 100;
  }

  public void udpateAnnotation(XYGateAnnotation priorAnnotation,
      XYGateAnnotation updatedAnnotation) {
    removeGateAnnotation(priorAnnotation);
    createGateAnnotation(updatedAnnotation);
  }

  public void createGateAnnotation(XYGateAnnotation annotation) {
    BitSet mask = ChartUtils.createGate(annotation).evaluate(data);
    String result = BitSetUtils.frequencyOfParent(mask, 2);
    double[] position = estimateGateLabelPosition(annotation);
    XYTextAnnotation label =
        new XYTextAnnotation(annotation.getSubsetName() + ": " + result, position[0], position[1]);
    label.setTextAnchor(TextAnchor.BASELINE_LEFT);
    label.setFont(new Font("Arial", Font.BOLD, 14));
    gateAnnotations.put(annotation, label);
    getChart().getXYPlot().addAnnotation(annotation,true);
    getChart().getXYPlot().addAnnotation(label);
  }

  private double[] estimateGateLabelPosition(XYGateAnnotation annotation) {
    Range xRange = annotation.getXRange();
    Range yRange = annotation.getYRange();
    double x = xRange.getLowerBound();
    double y = yRange.getUpperBound() + yHandleSize / 10;
    return new double[] {x, y};
  }

  public void removeGateAnnotation(XYAnnotation annotation) {
    XYTextAnnotation label = gateAnnotations.get(annotation);
    getChart().getXYPlot().removeAnnotation(label);
    gateAnnotations.remove(annotation);
    getChart().getXYPlot().removeAnnotation(annotation);
  }

  public void removeTemporaryAnnotation(XYAnnotation annotation) {
    this.getChart().getXYPlot().removeAnnotation(annotation);
  }

  public void addTemporaryAnnotation(XYAnnotation annotation) {
    this.getChart().getXYPlot().addAnnotation(annotation, true);
  }

  public void setSelectAnnotations(Point2D p) {
    selectedAnnotations.clear();
    // Split annotations into selected and unselected lists
    Map<Boolean, List<XYGateAnnotation>> gateSelection = gateAnnotations.keySet().stream()
        .collect(Collectors.partitioningBy(annotation -> annotation.containsPoint(p)));

    selectedAnnotations = gateSelection.get(new Boolean(true)).stream()
        .map(annotation -> updateSelectionStatus(annotation, true)).collect(Collectors.toList());

    // unselected annotations get selecetion cleared
    gateSelection.get(new Boolean(false))
        .forEach(annotation -> updateSelectionStatus(annotation, false));
  }

  private XYGateAnnotation updateSelectionStatus(XYGateAnnotation priorAnnotation,
      boolean markSelected) {
    XYGateAnnotation udpatedAnnotation;
    if (markSelected) {
      udpatedAnnotation = priorAnnotation.cloneSelected();
    } else {
      udpatedAnnotation = priorAnnotation.cloneDefault();
    }
    if (priorAnnotation != null) {
      udpateAnnotation(priorAnnotation, udpatedAnnotation);
    }
    return udpatedAnnotation;
  }

  public void translateSelectedAnnotations(double dx, double dy) {
    if (selectedAnnotations != null && selectedAnnotations.size() >= 1) {
      List<XYGateAnnotation> translatedAnnoations = selectedAnnotations.stream()
          .map(annotation -> moveAnnotation(annotation, dx, dy)).collect(Collectors.toList());
      selectedAnnotations = translatedAnnoations;
    }
  }

  private XYGateAnnotation moveAnnotation(XYGateAnnotation priorAnnotation, double dx, double dy) {
    XYGateAnnotation udpatedAnnotation = priorAnnotation.translate(dx, dy);
    udpateAnnotation(priorAnnotation, udpatedAnnotation);
    return udpatedAnnotation;
  }

  public void deleteSelectedAnnotations() {
    selectedAnnotations.forEach(annotation -> removeGateAnnotation(annotation));
  }

  public boolean hasGatesAtPoint(Point2D v) {
    List<XYGateAnnotation> matchingVertices = selectedAnnotations.stream()
        .filter(annotation -> annotation.matchesVertex(v, xHandleSize, yHandleSize))
        .collect(Collectors.toList());
    if (matchingVertices.size() >= 1) {
      return true;
    }
    return false;
  }

  public void adjustGatesAtPoint(Point2D v, double dx, double dy) {
    if (selectedAnnotations != null && selectedAnnotations.size() >= 1) {
      List<XYGateAnnotation> adjustedAnnotrations = selectedAnnotations.stream()
          .map(annotation -> updateGateVertex(annotation, v, xHandleSize, yHandleSize, dx, dy))
          .collect(Collectors.toList());
      selectedAnnotations = adjustedAnnotrations;
    }
  }

  private XYGateAnnotation updateGateVertex(XYGateAnnotation oldAnn, Point2D v, double xHandleSize,
      double yHandleSize, double dx, double dy) {
    XYGateAnnotation newAnn = oldAnn.updateVertex(v, dx, dy, xHandleSize, yHandleSize);
    udpateAnnotation(oldAnn, newAnn);
    return newAnn;
  }

  public String getDomainAxisName() {
    return this.spec.getDomainAxisName();
  }

  public String getRangeAxisName() {
    return this.spec.getRangeAxisName();
  }

  public void activateGateSelectButton() {
    ActionListener[] action = selectionButton.getActionListeners();
    for (ActionListener act : action) {
      if (act instanceof SelectionButtonListener) {
        ActionEvent event = new ActionEvent(selectionButton, 42, "What was the question again?");//TODO: Are these args relevant?
        act.actionPerformed(event);
      }
    }
  }

  public void setSelectionListener(JButton button) {
    this.selectionButton = button;
  }
  
  public List<AbstractGate> createAbstractGates(){
    
    List<AbstractGate> gates = gateAnnotations
                                          .keySet()
                                          .stream()
                                          .map(ann -> ChartUtils.createGate(ann))
                                          .collect(Collectors.toList());
    
    gates.forEach(gate -> gate.setParentID(data.getID()));
    
    return gates;
  }
  
  public void createAnnotations(List<AbstractGate> gates){
    gates
      .stream()
      .map(gate -> ChartUtils.createAnnotation(gate))
      .forEach(annotation -> createGateAnnotation(annotation));
  }

  public FCSFrame getDataFrame() {
    return this.data;
  }
}
