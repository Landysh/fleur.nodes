package io.landysh.inflor.main.knime.nodes.compensate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.ui.CellLineageTree;
import io.landysh.inflor.main.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CompensateNodeDialog extends DataAwareNodeDialogPane {

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private CompensateNodeSettings mSettings;

  private JPanel mAnalyisTab;
  private JComboBox<String> fcsColumnBox;
  private JComboBox<FCSFrame> selectSampleBox;

  protected CompensateNodeDialog() {
    super();
    mSettings = new CompensateNodeSettings();
    // Main analysis Tab
    mAnalyisTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    mAnalyisTab.setLayout(borderLayout);
    mAnalyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);
    super.addTab("From FCS File", mAnalyisTab);
  }

  private JPanel createOptionsPanel() {
    final JPanel optionsPanel = new JPanel();
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    optionsPanel.setLayout(layout);
    optionsPanel.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sample Options"));
    optionsPanel.add(Box.createVerticalGlue());
    optionsPanel.add(Box.createHorizontalGlue());
    // Select Input data
    fcsColumnBox = new JComboBox<>(new String[] {NO_COLUMNS_AVAILABLE_WARNING});
    fcsColumnBox.setSelectedIndex(0);
    fcsColumnBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mSettings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem());
      }
    });
    optionsPanel.add(fcsColumnBox);
    // Select file
    selectSampleBox = new JComboBox<>();
    selectSampleBox.setSelectedIndex(-1);
    selectSampleBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        FCSFrame fcs = (FCSFrame) selectSampleBox.getSelectedItem();
        parseSpillover(fcs);
      }


    });
    optionsPanel.add(selectSampleBox);
    return optionsPanel;
  }

  private void parseSpillover(FCSFrame fcs) {
    mSettings.setHeader(fcs.getKeywords());
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    final DataTableSpec spec = specs[0];

    // Update selected column Combo box
    fcsColumnBox.removeAllItems();
    for (String name : spec.getColumnNames()) {
      if (spec.getColumnSpec(name).getType() == FCSFrameFileStoreDataCell.TYPE) {
        fcsColumnBox.addItem(name);
      }
    }
    if (fcsColumnBox.getModel().getSize() == 0) {
      fcsColumnBox.addItem(NO_COLUMNS_AVAILABLE_WARNING);
    }
  }

  @Override
  protected void loadSettingsFrom(final NodeSettingsRO settings, final BufferedDataTable[] input)
      throws NotConfigurableException {

    DataTableSpec[] specs = {input[0].getSpec()};
    loadSettingsFrom(settings, specs);

    // Update Sample List
    String targetColumn = mSettings.getSelectedColumn();
    String[] names = input[0].getSpec().getColumnNames();
    int fcsColumnIndex = -1;
    for (int i = 0; i < names.length; i++) {
      if (names[i].matches(targetColumn)) {
        fcsColumnIndex = i;
      }
    }
    if (fcsColumnIndex == -1) {
      throw new NotConfigurableException("target column not in column list");
    }

    // read the sample names
    BufferedDataTable table = input[0];
    selectSampleBox.removeAllItems();

    // Hold on to a reference of the data so we can plot it later.

    HashSet<String> parameterSet = new HashSet<>();

    for (DataRow row : table) {
      FCSFrame cStoreData = ((FCSFrameFileStoreDataCell) row.getCell(fcsColumnIndex)).getFCSFrameValue();
      selectSampleBox.addItem(cStoreData);
      List<String> newParameters =cStoreData.getColumnNames();
      parameterSet.addAll(newParameters);
    }
    if (selectSampleBox.getModel().getSize() == 0) {
      selectSampleBox.removeAllItems();
      selectSampleBox.setEnabled(false);
      selectSampleBox.setToolTipText("No FCS Files found");
    }
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    mSettings.save(settings);
  }

  public FCSFrame getSelectedSample() {
    return (FCSFrame) this.selectSampleBox.getSelectedItem();
  }
}