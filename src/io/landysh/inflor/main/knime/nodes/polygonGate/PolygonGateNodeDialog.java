package io.landysh.inflor.main.knime.nodes.polygonGate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.gates.AbstractGate;
import io.landysh.inflor.main.core.gates.ui.GateCreationToolBar;
import io.landysh.inflor.main.core.plots.FCSChartPanel;
import io.landysh.inflor.main.core.utils.FCSUtilities;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameCell;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class PolygonGateNodeDialog extends DataAwareNodeDialogPane {

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private static final Integer DEFAULT_SUMMARY_FRAME_EVENT_COUNT = 10000;

  public PolygonGateSettings modelSettings;

  JPanel m_analyisTab;
  JComboBox<String> fcsColumnBox;
  JComboBox<FCSFrame> selectSampleBox;
  private ArrayList<FCSFrame> dataSet;

  protected PolygonGateNodeDialog() {
    super();
    modelSettings = new PolygonGateSettings();

    // Main analysis Tab
    m_analyisTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    m_analyisTab.setLayout(borderLayout);
    m_analyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);
    
    super.addTab("Analysis", m_analyisTab);
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
    fcsColumnBox = new JComboBox<String>(new String[] {NO_COLUMNS_AVAILABLE_WARNING});
    fcsColumnBox.setSelectedIndex(0);
    fcsColumnBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        modelSettings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem());
      }
    });
    optionsPanel.add(fcsColumnBox);

    // Select file
    selectSampleBox = new JComboBox<FCSFrame>();
    selectSampleBox.setSelectedIndex(-1);
    selectSampleBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
//TODO: something
        }
    });
    optionsPanel.add(selectSampleBox);
    return optionsPanel;
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
    if (fcsColumnBox.getModel().getSize() == 0) {
      fcsColumnBox.addItem(NO_COLUMNS_AVAILABLE_WARNING);
    }
  }

  @Override
  protected void loadSettingsFrom(final NodeSettingsRO settings, final BufferedDataTable[] input)
      throws NotConfigurableException {

    final DataTableSpec[] specs = {input[0].getSpec()};

    loadSettingsFrom(settings, specs);


    // Update Sample List
    final String targetColumn = modelSettings.getSelectedColumn();
    final String[] names = input[0].getSpec().getColumnNames();
    int fcsColumnIndex = -1;
    for (int i = 0; i < names.length; i++) {
      if (names[i].matches(targetColumn)) {
        fcsColumnIndex = i;
      }
    }
    if (fcsColumnIndex == -1) {
      throw new NotConfigurableException("target column not in column list");
    }

    // read the sample names;
    final BufferedDataTable table = input[0];
    selectSampleBox.removeAllItems();

    // Hold on to a reference of the data so we can plot it later.

    final HashSet<String> parameterSet = new HashSet<String>();
    
    dataSet = new ArrayList<FCSFrame>();
    
    for (DataRow row : table) {
      FCSFrame dataFrame = ((FCSFrameCell) row.getCell(fcsColumnIndex)).getFCSFrame();
      dataSet.add(dataFrame);
      List<String> newParameters = dataFrame.getColumnNames();
      parameterSet.addAll(newParameters);
    }
    FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(dataSet, DEFAULT_SUMMARY_FRAME_EVENT_COUNT);
    selectSampleBox.addItem(summaryFrame);
    dataSet.forEach(dataFrame -> selectSampleBox.addItem(dataFrame));
    selectSampleBox.setSelectedIndex(0);
  //TODO: something
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }

  public FCSFrame getSelectedSample() {
    return (FCSFrame) selectSampleBox.getSelectedItem();
  }

  public PolygonGateSettings getSettings() {
    return modelSettings;
  }
}