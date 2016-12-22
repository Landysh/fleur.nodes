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
package main.java.inflor.knime.nodes.transform;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.plots.PlotUtils;
import main.java.inflor.core.plots.SubsetResponseChart;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.transforms.LogicleTransform;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.knime.core.NodeUtilities;
import main.java.inflor.knime.data.type.cell.fcs.*;

/**
 * <code>NodeDialog</code> for the "Transform" Node.
 * 
 * @author Aaron Hart
 */

public class TransformNodeDialog extends DataAwareNodeDialogPane {

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private TransformNodeSettings modelSettings;
  private JPanel analysisTab;
  private JComboBox<String> fcsColumnBox;
  private ArrayList<FCSFrame> dataSet;
  private JPanel transformPanel;
  private JProgressBar progressBar;
  private JScrollPane scrollPane;

  protected TransformNodeDialog() {
    super();
    modelSettings = new TransformNodeSettings();
    // Main analysis Tab
    analysisTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    analysisTab.setLayout(borderLayout);
    analysisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);
    super.addTab("Transform Settings", analysisTab);
  }

  private void populateTransformPanel(JPanel panel) {
    panel.removeAll();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    for (String parameterName : modelSettings.getAllTransorms().navigableKeySet()) {
      ChartPanel chart = createTransformChart(parameterName);
      panel.add(chart, gbc);
      gbc.gridy++;
    }
  }

  private ChartPanel createTransformChart(String name) {
    HashMap<String, FCSDimension> dimensions = findMatchingDimensions(name);
    AbstractTransform currentTransform = modelSettings.getTransform(name);
    if (currentTransform == null) {
      currentTransform = new LogicleTransform();
    }
    SubsetResponseChart fcsChart = new SubsetResponseChart(name, currentTransform);
    JFreeChart chart = fcsChart.createChart(dimensions);
    ChartPanel panel = new ChartPanel(chart);
    panel.setPreferredSize(new Dimension(400, 200));
    return panel;
  }

  private HashMap<String, FCSDimension> findMatchingDimensions(String name) {
    HashMap<String, FCSDimension> result = new HashMap<String, FCSDimension>();
    for (FCSFrame dataFrame : dataSet) {
      String key = dataFrame.getPrefferedName();
      FCSDimension value = FCSUtilities.findCompatibleDimension(dataFrame, name);
      if (value!=null){
        result.put(key, value);
      }
    }
    return result;
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
    progressBar = new JProgressBar();
    optionsPanel.add(progressBar);
    return optionsPanel;
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    final DataTableSpec spec = specs[0];
    // Update selected column Combo box
    fcsColumnBox.removeAllItems();
    for (final String name : spec.getColumnNames()) {
      if (spec.getColumnSpec(name).getType() == FCSFrameFileStoreDataCell.TYPE) {
        fcsColumnBox.addItem(name);
      }
    }
    DataColumnSpec selectedColumnSpec = spec.getColumnSpec((String) fcsColumnBox.getSelectedItem());
    String shortNames = selectedColumnSpec.getProperties()
        .getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    String[] dimensionNames = shortNames.split(NodeUtilities.DELIMITER_REGEX);
    for (String name : dimensionNames) {
      if (modelSettings.getTransform(name) == null) {
        modelSettings.addTransform(name, PlotUtils.createDefaultTransform(name));
      }
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

    // Hold on to a reference of the data so we can plot it later.
    dataSet = new ArrayList<FCSFrame>();
    for (final DataRow row : table) {
      final FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) row.getCell(fcsColumnIndex)).getFCSFrameValue();
      dataSet.add(dataFrame);
    }

    transformPanel = new JPanel();
    populateTransformPanel(transformPanel);
    scrollPane = new JScrollPane(transformPanel);
    scrollPane.setPreferredSize(new Dimension(400, 600));
    analysisTab.add(scrollPane, BorderLayout.CENTER);
    modelSettings.optimizeTransforms(dataSet);
    updateTransformPanel();
  }

  protected void updateTransformPanel() {
    progressBar.setVisible(true);
    progressBar.setStringPainted(true);
    progressBar.setString("Initializing");
    progressBar.getModel().setValue(1);
    UpdateTransformPanelWorker worker =
        new UpdateTransformPanelWorker(progressBar, transformPanel, modelSettings, dataSet);
    worker.execute();
    try {
      ArrayList<ChartPanel> chartPanels = worker.get();
      transformPanel.removeAll();
      transformPanel.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      for (ChartPanel chart: chartPanels) {
        transformPanel.add(chart, gbc);
        gbc.gridy++;
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }
}
