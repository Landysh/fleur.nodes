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
package io.landysh.inflor.main.core.fcs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class FCSSummaryPanel extends JPanel {

  /**
   * Provides a simple summary of some keywords in an FCS Header
   */
  private static final long serialVersionUID = 1L;

  public FCSSummaryPanel(Map<String, String> keywords) {
    super(new GridBagLayout());
    super.setName("FCS Summary");
    final JTable table = new JTable(6, 2);
    final JTableHeader tableHeader = new JTableHeader();

    table.setTableHeader(tableHeader);
    table.setValueAt("Event Count", 0, 0);
    table.setValueAt(Integer.parseInt(keywords.get("$TOT")), 0, 1);

    table.setValueAt("Parameter Count", 1, 0);
    table.setValueAt(keywords.get("$PAR"), 1, 1);

    table.setValueAt("Date", 2, 0);
    table.setValueAt(keywords.get("$DATE"), 2, 1);

    table.setValueAt("Cytometer", 3, 0);
    table.setValueAt(keywords.get("$CYT"), 3, 1);

    table.setValueAt("Acquisition Start", 4, 0);
    table.setValueAt(keywords.get("$BTIM"), 4, 1);

    table.setValueAt("Acquisition End", 5, 0);
    table.setValueAt(keywords.get("$ETIM"), 5, 1);

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridx = 0;
    gbc.weightx = 1;
    gbc.gridy = 0;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;

    super.add(table, gbc);
    gbc.insets = new Insets(5, 0, 5, 5);
  }
}
