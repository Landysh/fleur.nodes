package io.landysh.inflor.main.core.fcs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class FCSSummaryPanel extends JPanel {

  /**
   * Provides a simple summary of some keywords in an FCS Header
   */
  private static final long serialVersionUID = 1L;

  public FCSSummaryPanel(HashMap<String, String> header) {
    super(new GridBagLayout());
    super.setName("FCS Summary");
    final JTable table = new JTable(6, 2);
    final JTableHeader tableHeader = new JTableHeader();

    table.setTableHeader(tableHeader);
    table.setValueAt("Event Count", 0, 0);
    table.setValueAt(Integer.parseInt(header.get("$TOT")), 0, 1);

    table.setValueAt("Parameter Count", 1, 0);
    table.setValueAt(header.get("$PAR"), 1, 1);

    table.setValueAt("Date", 2, 0);
    table.setValueAt(header.get("$DATE"), 2, 1);

    table.setValueAt("Cytometer", 3, 0);
    table.setValueAt(header.get("$CYT"), 3, 1);

    table.setValueAt("Acquisition Start", 4, 0);
    table.setValueAt(header.get("$BTIM"), 4, 1);

    table.setValueAt("Acquisition End", 5, 0);
    table.setValueAt(header.get("$ETIM"), 5, 1);

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
