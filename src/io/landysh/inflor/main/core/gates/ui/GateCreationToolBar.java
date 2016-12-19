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
   * This tool bar is responsible for managing the active listeners (gate drawing/zooming mode) on a
   * JFreeChart ChartPanel.
   */

  ArrayList<JButton> cursorButtons;
  transient MouseListener activeListener;
  private FCSChartPanel panel;
  private JButton selectButton;
  private JButton rectGateButton;
  private JButton polyGateButton;

  public GateCreationToolBar(FCSChartPanel panel) {
    super(TOOLBAR_TITLE);
    this.panel = panel;

    cursorButtons = new ArrayList<>();

    selectButton = createSelectButton();
    rectGateButton = createRectGateButton();
    polyGateButton = createPolyGateButton();

    cursorButtons = new ArrayList<>();
    cursorButtons.add(selectButton);
    cursorButtons.add(rectGateButton);
    cursorButtons.add(polyGateButton);
    cursorButtons.forEach(this::add);
  }

  private JButton createSelectButton() {
    GateSelectionAdapter gateSelectionAdapter = new GateSelectionAdapter(panel);
    selectButton = new JButton("Select");
    SelectionButtonListener sbl = new SelectionButtonListener(panel, this, gateSelectionAdapter);
    selectButton.addActionListener(sbl);
    return selectButton;
  }

  private JButton createRectGateButton() {
    RectangleGateAdapter rectGateMouseAdapter = new RectangleGateAdapter(panel);
    rectGateButton = new JButton("RectGate");
    rectGateButton.addActionListener(e -> {
        removeAllListeners();
        cursorButtons.forEach(button -> button.setEnabled(true));
        ((JButton) e.getSource()).setEnabled(false);
        panel.addMouseListener(rectGateMouseAdapter);
        panel.addMouseMotionListener((MouseMotionListener) rectGateMouseAdapter);
    });
    return rectGateButton;
  }

  private JButton createPolyGateButton() {
    PolygonGateAdapter polyGateMouseAdapter = new PolygonGateAdapter(panel);

    polyGateButton = new JButton("Polygon");
    polyGateButton.addActionListener(e -> {
        removeAllListeners();
        cursorButtons.forEach(button -> button.setEnabled(true));
        ((JButton) e.getSource()).setEnabled(false);
        panel.addMouseListener(polyGateMouseAdapter);
        panel.addMouseMotionListener((MouseMotionListener) polyGateMouseAdapter);
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
