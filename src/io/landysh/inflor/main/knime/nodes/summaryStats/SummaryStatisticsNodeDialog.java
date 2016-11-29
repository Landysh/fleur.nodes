package io.landysh.inflor.main.knime.nodes.summaryStats;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.main.knime.core.NodeUtilities;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameCell;

/**
 * <code>NodeDialog</code> for the "SummaryStatistics" Node. Extract basic summary statistics from a
 * set of FCS Files.
 * 
 * @author Aaron Hart
 */
public class SummaryStatisticsNodeDialog extends NodeDialogPane {
  
  SummaryStatsSettings modelSettings;
  private JComboBox<String> fcsColumnBox;
  private String[] subsetNames;
  private String[] dimensionNames;
  private JPanel statsTab;
  private JPanel statsPanel;
  private JTable table;
  
  public SummaryStatisticsNodeDialog() {
    super();
    modelSettings = new SummaryStatsSettings();
    statsTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    statsTab.setLayout(borderLayout);
    JPanel optionsPanel = createOptionsPanel();
    statsTab.add(optionsPanel, BorderLayout.NORTH);
    statsPanel = creasteStatsPanel();
    statsTab.add(statsPanel, BorderLayout.CENTER);
    super.addTab("Statistics", statsTab, true);
  }
  
  private JPanel creasteStatsPanel() {
    
    return new JPanel();
  }

  private JPanel createOptionsPanel() {
    JPanel optionsPanel = new JPanel();
    
    fcsColumnBox = new JComboBox<String>();
    fcsColumnBox.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        modelSettings.setSelectedColumn((String) fcsColumnBox.getSelectedItem());
        
      }
    });
    optionsPanel.add(fcsColumnBox);
    JButton addButton = new JButton("+");
    addButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        popStatEditorDialog();
      }
    });
    optionsPanel.add(addButton);
    
    JButton removeButton = new JButton("-");
    removeButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        if (row!=-1&&column!=-1){
          StatSpec spec = (StatSpec) table.getValueAt(row, column);
          modelSettings.removeStat(spec);
          DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
          tableModel.removeRow(row);
        }
      }
    });
    optionsPanel.add(removeButton);

    return optionsPanel;
  }

  protected void popStatEditorDialog() {
     Window topFrame = SwingUtilities.getWindowAncestor(this.getPanel());

    StatEditorDialog dialog = new StatEditorDialog(topFrame, Arrays.asList(dimensionNames), Arrays.asList(subsetNames));
    dialog.setVisible(true);
    if (dialog.isOK) {
      modelSettings.addStatSpec(dialog.getStatSpec());
      updateStatsPanel();
    }
    dialog.dispose();
  }

  private void updateStatsPanel() {
    if (this.statsPanel!=null){
      this.statsTab.remove(statsPanel);
      statsPanel = new JPanel();
      List<StatSpec> statSpecs = modelSettings.getStatSpecs();
      //List<String> statTable = statSpecs.stream().map(spec -> spec.toString()).collect(Collectors.toList());
      String[] columnNames = new String[]{"Summary"};
      DefaultTableModel model = new DefaultTableModel(columnNames, 0);
      
      statSpecs
        .stream()
        .map(str -> new StatSpec[]{str})
        .forEach(row -> model.addRow(row));
      
      table = new JTable(model);
      table.setDefaultEditor(Object.class, null);

      JScrollPane scrollPane = new JScrollPane(table);
      statsPanel.add(scrollPane, BorderLayout.CENTER);
      statsTab.add(statsPanel, BorderLayout.CENTER);
      statsTab.revalidate();
    }
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }
  
  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    final DataTableSpec spec = specs[0];
    // Update selected column Combo box
    fcsColumnBox.removeAllItems();
    for (final String name : spec.getColumnNames()) {
      if (spec.getColumnSpec(name).getType() == FCSFrameCell.TYPE) {
        fcsColumnBox.addItem(name);
      }
    }
    
    DataColumnSpec selectedColumnSpec = spec.getColumnSpec((String) fcsColumnBox.getSelectedItem());
    String shortNames = selectedColumnSpec.getProperties()
        .getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    dimensionNames = shortNames.split(NodeUtilities.DELIMITER_REGEX);
    
    String subsetNamesString = selectedColumnSpec.getProperties()
        .getProperty(NodeUtilities.SUBSET_NAMES_KEY);
    subsetNames = null;
    if (subsetNamesString!=null){
      subsetNames = subsetNamesString.split(NodeUtilities.DELIMITER_REGEX);
    } else {
      subsetNames = new String[] {"Ungated"};
    }
  }
}
