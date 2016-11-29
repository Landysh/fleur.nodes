package io.landysh.inflor.main.core.gates.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import io.landysh.inflor.main.core.plots.FCSChartPanel;

@SuppressWarnings("serial")
public class GateCreationToolBar extends JToolBar {
  private static final String TOOLBAR_TITLE = "Mouse Mode";
  /**
   * This toolbar is responsible for managing the active listeners (gate drawing/zooming mode) on a
   * JFreeChart ChartPanel.
   */

  ArrayList<JButton> cursorButtons;
  MouseListener activeListener;
  private FCSChartPanel panel;
  private JButton selectButton;
  private JButton rectGateButton;
  private JButton polyGateButton;
  private GateSelectionAdapter gateSelectionAdapter;
  private RectangleGateAdapter rectGateMouseAdapter;
  private PolygonGateAdapter polyGateMouseAdapter;

  public GateCreationToolBar(FCSChartPanel panel) {
    super(TOOLBAR_TITLE);
    this.panel = panel;

    cursorButtons = new ArrayList<JButton>();

    selectButton = createSelectButton();
    rectGateButton = createRectGateButton();
    polyGateButton = createPolyGateButton();

    cursorButtons = new ArrayList<JButton>();
    cursorButtons.add(selectButton);
    cursorButtons.add(rectGateButton);
    cursorButtons.add(polyGateButton);
    cursorButtons.forEach(button -> this.add(button));
  }

  private JButton createSelectButton() {
    gateSelectionAdapter = new GateSelectionAdapter(panel);
    selectButton = new JButton("Select");
    SelectionButtonListener sbl = new SelectionButtonListener(panel, this, gateSelectionAdapter);
    selectButton.addActionListener(sbl);
    return selectButton;
  }

  private JButton createRectGateButton() {
    rectGateMouseAdapter = new RectangleGateAdapter(panel);
    rectGateButton = new JButton("RectGate");
    rectGateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        removeAllListeners();

        cursorButtons.forEach(button -> button.setEnabled(true));
        ((JButton) arg0.getSource()).setEnabled(false);

        panel.addMouseListener(rectGateMouseAdapter);
        panel.addMouseMotionListener((MouseMotionListener) rectGateMouseAdapter);
      }
    });
    return rectGateButton;
  }

  private JButton createPolyGateButton() {
    polyGateMouseAdapter = new PolygonGateAdapter(panel);

    polyGateButton = new JButton("Polygon");
    polyGateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        removeAllListeners();

        cursorButtons.forEach(button -> button.setEnabled(true));
        ((JButton) arg0.getSource()).setEnabled(false);

        panel.addMouseListener(polyGateMouseAdapter);
        panel.addMouseMotionListener((MouseMotionListener) polyGateMouseAdapter);
      }
    });
    return polyGateButton;
  }

  void removeAllListeners() {
    EventListener[] motion = panel.getListeners(MouseMotionListener.class);
    for (EventListener el : motion) {
      panel.removeMouseMotionListener((MouseMotionListener) el);
    }
    EventListener[] listener = panel.getListeners(MouseListener.class);
    for (EventListener l : listener) {
      panel.removeMouseListener((MouseListener) l);
    }
  }

  public JButton getSelectionListener() {
    return selectButton;
  }
}
