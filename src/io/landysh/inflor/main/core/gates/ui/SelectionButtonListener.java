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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import io.landysh.inflor.main.core.plots.FCSChartPanel;

public class SelectionButtonListener implements ActionListener {

  FCSChartPanel panel;
  GateCreationToolBar toolbar;
  private GateSelectionAdapter mouseAdapter;

  public SelectionButtonListener(FCSChartPanel panel, GateCreationToolBar toolbar,
      GateSelectionAdapter mouseAdapter) {
    this.panel = panel;
    this.toolbar = toolbar;
    this.mouseAdapter = mouseAdapter;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    toolbar.removeAllListeners();

    toolbar.cursorButtons.forEach(button -> button.setEnabled(true));
    ((JButton) e.getSource()).setEnabled(false);

    panel.addMouseListener(mouseAdapter);
    panel.addMouseMotionListener(mouseAdapter);
  }
}
