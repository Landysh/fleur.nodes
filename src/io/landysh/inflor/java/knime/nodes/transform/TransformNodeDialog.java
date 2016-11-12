package io.landysh.inflor.java.knime.nodes.transform;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
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

import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.plots.PlotUtils;
import io.landysh.inflor.java.core.plots.SubsetResponseChart;
import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicTransform;
import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.core.utils.MatrixUtilities;
import io.landysh.inflor.java.knime.core.NodeUtilities;
import io.landysh.inflor.java.knime.dataTypes.FCSFrameCell.FCSFrameCell;

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

    JButton optimizeWButton = new JButton("Optimize");
    optimizeWButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        optimizeTransforms();
        updateTransformPanel();
      }
    });
    optionsPanel.add(optimizeWButton);
    progressBar = new JProgressBar();
    optionsPanel.add(progressBar);
    return optionsPanel;
  }

  private void optimizeTransforms() {
    for (Entry<String, AbstractTransform> entry : modelSettings.getAllTransorms().entrySet()) {
      double[] data = mergeData(entry.getKey(), dataSet);
      if (entry.getValue() instanceof LogicleTransform) {
        LogicleTransform logicle = (LogicleTransform) entry.getValue();
        logicle.optimizeW(data);
      } else if (entry.getValue() instanceof LogrithmicTransform) {
        LogrithmicTransform logTransform = (LogrithmicTransform) entry.getValue();
        logTransform.optimize(data);
      } else if (entry.getValue() instanceof BoundDisplayTransform) {
        BoundDisplayTransform boundaryTransform = (BoundDisplayTransform) entry.getValue();
        boundaryTransform.optimize(data);
      }
    }
  }

  private double[] mergeData(String shortName, ArrayList<FCSFrame> dataSet2) {
    double[] data = null;
    for (FCSFrame frame : dataSet2) {
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(frame, shortName);
      data = MatrixUtilities.appendVectors(data, dimension.getData());
    }
    return data;
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
      final FCSFrame dataFrame = ((FCSFrameCell) row.getCell(fcsColumnIndex)).getFCSFrame();
      dataSet.add(dataFrame);
    }

    transformPanel = new JPanel();
    populateTransformPanel(transformPanel);
    scrollPane = new JScrollPane(transformPanel);
    scrollPane.setPreferredSize(new Dimension(400, 600));
    analysisTab.add(scrollPane, BorderLayout.CENTER);
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
