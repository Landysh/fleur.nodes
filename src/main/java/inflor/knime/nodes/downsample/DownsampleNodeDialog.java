/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on February 27, 2017 by Aaron Hart
 */
package inflor.knime.nodes.downsample;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import inflor.core.downsample.DownSampleMethods;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "Transform" Node.
 * 
 * @author Aaron Hart
 */

public class DownsampleNodeDialog extends NodeDialogPane {

  private static final NodeLogger logger = NodeLogger.getLogger(DownsampleNodeDialog.class);

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private static final String KEY_OPTIONS_TAB = "Options";

  private DownsampleNodeSettings modelSettings;
  private JPanel optionsTabPanel;
  private JComboBox<String> fcsColumnBox;
  private JComboBox<String> subsetSelectionBox;

  private String[] displayNames;
  private String[] shortNames;
  private String[] subsetNames;

  private JPanel detailsPanel;

  protected DownsampleNodeDialog() {
    super();
    modelSettings = new DownsampleNodeSettings();
    // Main analysis Tab
    optionsTabPanel = new JPanel(new BorderLayout());
    optionsTabPanel.add(columnSelectionPanel(), BorderLayout.NORTH);
    updateDetailsPanel();
    optionsTabPanel.setPreferredSize(new Dimension(400, 600));
    super.addTab(KEY_OPTIONS_TAB, optionsTabPanel);

  }

  private void updateDetailsPanel() {
    if (detailsPanel != null) {
      optionsTabPanel.remove(detailsPanel);
    }
    JPanel newPanel = (JPanel) downsampleDetailsPanel();
    optionsTabPanel.add(newPanel, BorderLayout.CENTER);
    optionsTabPanel.revalidate();
    optionsTabPanel.repaint(50l);
  }

  private Component downsampleDetailsPanel() {
    detailsPanel = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    detailsPanel.setLayout(borderLayout);
    JComboBox<DownSampleMethods> methodBox = new JComboBox<>();
    Arrays.asList(DownSampleMethods.values()).forEach(methodBox::addItem);
    methodBox.setSelectedItem(modelSettings.getSampleMethod());
    methodBox.addActionListener(e -> {

      DownSampleMethods newMethod = (DownSampleMethods) methodBox.getSelectedItem();
      modelSettings.setSelectedDownsampleMethod(newMethod);
      updateDetailsPanel();
    });
    detailsPanel.add(methodBox, BorderLayout.NORTH);
    JPanel settingsPanel = createDetailSettingsPanel();
    detailsPanel.add(settingsPanel, BorderLayout.CENTER);
    return detailsPanel;
  }

  private JPanel createDetailSettingsPanel() {
    JPanel dsp = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    if (modelSettings.getSampleMethod().equals(DownSampleMethods.RANDOM)) {
      SpinnerModel ceilSpinnerModel =
          new SpinnerNumberModel((Number) modelSettings.getCeiling(), 1, Integer.MAX_VALUE, 1);
      JSpinner ceilingSpinner = new JSpinner(ceilSpinnerModel);
      ceilingSpinner.addChangeListener(e -> {
        modelSettings.setRandomSeed((Integer) ceilingSpinner.getModel().getValue());
      });

      dsp.add(ceilingSpinner, gbc);
      gbc.gridy++;
      JCheckBox useRandomSeed = new JCheckBox("Use random seed.", modelSettings.isRandomSeed());
      useRandomSeed.addChangeListener(e -> {
        modelSettings.setUseRandomSeed(useRandomSeed.isSelected());
        updateDetailsPanel();
        dsp.add(useRandomSeed, gbc);
      });
      if (!modelSettings.isRandomSeed()) {
        SpinnerModel spinnerModel = new SpinnerNumberModel(modelSettings.getRandomSeed(),
            Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        JSpinner seedSpinner = new JSpinner(spinnerModel);
        seedSpinner.addChangeListener(e -> {
          modelSettings.setRandomSeed((Integer) seedSpinner.getModel().getValue());
        });
        gbc.gridy++;
        dsp.add(seedSpinner, gbc);
      }
    } else if (modelSettings.getSampleMethod().equals(DownSampleMethods.DENSITY_DEPENDENT)
        && shortNames != null && displayNames != null) {
      JList<String> selectedDimensionsList = new JList<>(displayNames);// TODO Nicer if this were
                                                                       // directly on dimensions.
      selectedDimensionsList.getSelectionModel().addListSelectionListener(e -> {
        int[] selectedIndicies = selectedDimensionsList.getSelectedIndices();
        String[] selectedShortNames = new String[selectedIndicies.length];
        for (int i = 0; i < selectedIndicies.length; i++) {
          selectedShortNames[i] = shortNames[i];
        }
        modelSettings.setDimensionNames(selectedShortNames);
      });

      dsp.add(selectedDimensionsList, gbc);
    }
    return dsp;
  }

  private JPanel columnSelectionPanel() {
    final JPanel columnSelectionPanel = new JPanel();
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    columnSelectionPanel.setLayout(layout);
    columnSelectionPanel.setBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Source"));
    columnSelectionPanel.add(Box.createVerticalGlue());
    columnSelectionPanel.add(Box.createHorizontalGlue());
    // Select Input data
    fcsColumnBox = new JComboBox<>();
    fcsColumnBox.addActionListener(
        e -> modelSettings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem()));
    columnSelectionPanel.add(fcsColumnBox);
    subsetSelectionBox = new JComboBox<>();   	
    subsetSelectionBox.addActionListener(e -> modelSettings.setReferenceSubset((String) subsetSelectionBox.getSelectedItem()));
    columnSelectionPanel.add(subsetSelectionBox);
    return columnSelectionPanel;
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) {
    final DataTableSpec spec = specs[0];
    // Update selected column Combo box
    fcsColumnBox.removeAllItems();
    for (final String name : spec.getColumnNames()) {
      if (spec.getColumnSpec(name).getType() == FCSFrameFileStoreDataCell.TYPE) {
        fcsColumnBox.addItem(name);
      }
    }
    subsetSelectionBox.removeAllItems();
    subsetSelectionBox.addItem(DownsampleNodeSettings.DEFAULT_REFERENCE_SUBSET);
    if (subsetNames!=null){
    	Arrays.asList(subsetNames).forEach(subsetSelectionBox::addItem);
    }
    subsetSelectionBox.setSelectedItem(modelSettings.getReferenceSubset());



    DataColumnProperties props =
        spec.getColumnSpec((String) fcsColumnBox.getSelectedItem()).getProperties();

    displayNames =
        props.getProperty(NodeUtilities.DISPLAY_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
    String s2 = props.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    shortNames = s2.split(NodeUtilities.DELIMITER_REGEX);
    if (props.containsProperty(NodeUtilities.SUBSET_NAMES_KEY)){
    	subsetNames = props.getProperty(NodeUtilities.SUBSET_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
    }
    optionsTabPanel.revalidate();
    optionsTabPanel.repaint(50l);
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }
}
