package main.java.inflor.knime.nodes.compensation.calculate;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.ui.ButtonColumn;

/**
 * <code>NodeDialog</code> for the "CalculateCompensation" Node. This node attempts to construct a
 * compensation matrix automatically using heuristics to estimate sample roles and Theil-Sen
 * estimation to calculate individual spillover values.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */

public class CalculateCompensationNodeDialog extends NodeDialogPane {

  private static final NodeLogger logger =
      NodeLogger.getLogger(CalculateCompensationNodeDialog.class);

  private static final Object[] COMP_MAP_TABLE_COLUMN_NAMES =
      new String[] {"Marker", "Compensation Control", "Remove"};

  private CalculateCompensationNodeSettings mSettings;
  
  private JPanel compMapPanel = new JPanel();


  private JComboBox<String> comboBox = new JComboBox<>();
  private JFileChooser fileChooser = new JFileChooser(".");
  private JButton browseButton = new JButton("Browse");

  protected CalculateCompensationNodeDialog() {
    super();
    mSettings = new CalculateCompensationNodeSettings();
    // Main analysis Tab
    JPanel firstTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    firstTab.setLayout(borderLayout);
    firstTab.add(createFileChooser(), BorderLayout.NORTH);
    firstTab.add(compMapPanel, BorderLayout.CENTER);
    super.addTab("Control Mapping", firstTab);
    if (!mSettings.getPath().equals(CalculateCompensationNodeSettings.DEFAULT_PATH)) {
      updateCompensationMapPanel();
    }
    firstTab.revalidate();
    firstTab.repaint();
  }

  private Component createFileChooser() {
    JPanel panel = new JPanel();
    comboBox.setPreferredSize(new Dimension(200, (int) comboBox.getPreferredSize().getHeight()));

    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    browseButton.addActionListener(e -> {
      int status = fileChooser.showOpenDialog(panel);
      if (status == JFileChooser.APPROVE_OPTION) {
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        comboBox.addItem(path);
        comboBox.setSelectedItem(path);
      }
    });
    comboBox.addActionListener(e -> {
      String path = (String) comboBox.getSelectedItem();
      mSettings.setPath(path);
      updateCompensationMapPanel();
    });
    panel.add(comboBox);
    panel.add(browseButton);
    return panel;
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    if (!mSettings.getPath().equals(CalculateCompensationNodeSettings.DEFAULT_PATH)) {
      try {
        mSettings.load(settings);
        updateCompensationMapPanel(); 
      } catch (InvalidSettingsException e) {
        throw new NotConfigurableException("Failed to load Settings.", e);
      }

    } else {
      // noop
    }
  }

  private void updateCompensationMapPanel() {

    compMapPanel.removeAll();
    //Create table model
    DefaultTableModel tM = new DefaultTableModel();
    Object[][] tableContent = createTableContent();
    tM.setDataVector(tableContent, COMP_MAP_TABLE_COLUMN_NAMES);
    JTable table = new JTable(tM);
    table.getColumnModel().getColumn(0).setPreferredWidth(100);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);
    table.getColumnModel().getColumn(2).setPreferredWidth(50);

    //Setup combo box for file selector. 
    JComboBox<String> fcsFrameComboBox = new JComboBox<>();
    mSettings.getFileNames().forEach(fcsFrameComboBox::addItem);
    fcsFrameComboBox.addActionListener(e -> {
      int modelRow = table.getSelectedRow();
      if (modelRow >=0){
        String dimension = (String) table.getModel().getValueAt(modelRow, 0);
        String newValue = (String) table.getModel().getValueAt(modelRow, 1);
        mSettings.modifyDimension(dimension, newValue);
      }
    });
    table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(fcsFrameComboBox));

    //Create button column.
    @SuppressWarnings("serial")
    Action delete = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JTable table = (JTable) e.getSource();
        int modelRow = Integer.parseInt(e.getActionCommand());
        mSettings.removeDimension((String) table.getModel().getValueAt(modelRow, 0));
        ((DefaultTableModel) table.getModel()).removeRow(modelRow);
      }
    };
    ButtonColumn buttonColumn = new ButtonColumn(table, delete, 2);
    buttonColumn.setMnemonic(KeyEvent.VK_D);

    //update it all.
    compMapPanel.add(table);
    compMapPanel.revalidate();
    compMapPanel.repaint();
  }

  private Object[][] createTableContent() {
    Map<String, FCSFrame> compMap = mSettings.getCompMap();
    Object[][] table = new Object[compMap.size()][3];
    int i = 0;
    for (Entry<String, FCSFrame> entry : compMap.entrySet()) {
      JButton button = new JButton(entry.getKey());
      button.addActionListener(e -> {
        JButton sourceButton = (JButton) e.getSource();
        mSettings.removeDimension(sourceButton.getText());
      });
      table[i] = new Object[] {entry.getKey(), entry.getValue().getPrefferedName(), button};
      i++;
    }
    return table;
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    mSettings.save(settings);
  }
}
