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
package fleur.knime.nodes.downsample;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.ListSelectionListener;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import fleur.core.sample.DownSampleMethods;
import fleur.knime.core.NodeUtilities;
import fleur.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "Transform" Node.
 * 
 * @author Aaron Hart
 */

public class DownsampleNodeDialog extends NodeDialogPane {

  private static final String KEY_OPTIONS_TAB = "Options";

  private DownsampleNodeSettings modelSettings;
  private JPanel optionsTabPanel;
  private JComboBox<String> fcsColumnBox;
  private ActionListener fcsbal =
      e -> modelSettings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem());

  private JComboBox<String> subsetSelectionBox;
  private ActionListener ssbal = e -> updateSubsetBox(e);

  private String[] displayNames;
  private String[] shortNames;
  private String[] subsetNames;

  private JPanel detailsPanel;
  private JComboBox<DownSampleMethods> methodBox;
  private ActionListener methodBoxListener = e -> updateMethodBox();

  JList<String> selectedDimensionsList;
  private ListSelectionListener selectedDimsListener = e -> updateSelectedDimensions();

  private JSpinner ceilingSpinner;


  private void updateMethodBox() {
    DownSampleMethods newMethod = (DownSampleMethods) methodBox.getSelectedItem();
    modelSettings.setSelectedDownsampleMethod(newMethod);
    updateDetailsPanel();
  }


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
    methodBox = new JComboBox<>();
    Arrays.asList(DownSampleMethods.values()).forEach(methodBox::addItem);
    methodBox.setSelectedItem(modelSettings.getSampleMethod());
    methodBox.addActionListener(methodBoxListener);
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
      ceilingSpinner = new JSpinner(ceilSpinnerModel);
      ceilingSpinner.addChangeListener(e -> {
        modelSettings.setCeiling((Integer) ceilingSpinner.getModel().getValue());
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
      selectedDimensionsList = new JList<>(displayNames);// TODO Nicer if this were
                                                         // directly on dimensions.
      selectedDimensionsList.getSelectionModel().addListSelectionListener(selectedDimsListener);

      dsp.add(selectedDimensionsList, gbc);
    }
    return dsp;
  }


  private void updateSelectedDimensions() {
    int[] selectedIndicies = selectedDimensionsList.getSelectedIndices();
    String[] selectedShortNames = new String[selectedIndicies.length];
    for (int i = 0; i < selectedIndicies.length; i++) {
      selectedShortNames[i] = shortNames[selectedIndicies[i]];
    }
    modelSettings.setDimensionNames(selectedShortNames);
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
    columnSelectionPanel.add(fcsColumnBox);
    subsetSelectionBox = new JComboBox<>();
    columnSelectionPanel.add(subsetSelectionBox);
    return columnSelectionPanel;
  }

  private void updateSubsetBox(ActionEvent e) {
    String selection = (String) subsetSelectionBox.getSelectedItem();
    if (selection != null) {
      modelSettings.setReferenceSubset(selection);
    }
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) {
    // load the model settings.
    try {
      modelSettings.load(settings);
    } catch (InvalidSettingsException e) {
      getLogger().error("Unable to load settings", e);
    }
    final DataTableSpec spec = specs[0];


    // Update selected column Combo box
    fcsColumnBox.removeActionListener(fcsbal);
    fcsColumnBox.removeAllItems();
    for (final String name : spec.getColumnNames()) {
      if (spec.getColumnSpec(name).getType() == FCSFrameFileStoreDataCell.TYPE) {
        fcsColumnBox.addItem(name);
      }
    }
    fcsColumnBox.addActionListener(fcsbal);
    if (modelSettings.getSelectedColumn() != DownsampleNodeSettings.DEFAULT_SELECTED_COLUMN) {
      fcsColumnBox.setSelectedItem(modelSettings.getSelectedColumn());
    } else {
      fcsColumnBox.setSelectedIndex(0);
    }
    // Pull relevant info from the spec.
    DataColumnProperties props =
        spec.getColumnSpec((String) fcsColumnBox.getSelectedItem()).getProperties();
    displayNames =
        props.getProperty(NodeUtilities.DISPLAY_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
    String s2 = props.getProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    shortNames = s2.split(NodeUtilities.DELIMITER_REGEX);
    if (props.containsProperty(NodeUtilities.SUBSET_NAMES_KEY)) {
      subsetNames =
          props.getProperty(NodeUtilities.SUBSET_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
    }

    // Update selected column Combo box
    subsetSelectionBox.removeActionListener(ssbal);
    subsetSelectionBox.removeAllItems();
    subsetSelectionBox.addItem(DownsampleNodeSettings.DEFAULT_REFERENCE_SUBSET);
    if (subsetNames != null) {
      Arrays.asList(subsetNames).forEach(subsetSelectionBox::addItem);
    }
    subsetSelectionBox.setSelectedItem(modelSettings.getReferenceSubset());
    subsetSelectionBox.addActionListener(ssbal);

    ceilingSpinner.getModel().setValue(modelSettings.getCeiling());

    // Update downsample methods box;
    methodBox.setSelectedItem(modelSettings.getSampleMethod());
    if (methodBox.getSelectedItem().equals(DownSampleMethods.DENSITY_DEPENDENT)) {
      String[] selectedDimensions = modelSettings.getDimensionNames();
      int[] indices = new int[selectedDimensions.length];
      for (int i = 0; i < indices.length; i++) {
        for (int j = 0; j < shortNames.length; j++) {
          String s1 = selectedDimensions[i];
          String s3 = shortNames[j];
          if (s1.equals(s3)) {
            indices[i] = j;
          }
        }
      }
      selectedDimensionsList.setSelectedIndices(indices);
    }
    optionsTabPanel.revalidate();
    optionsTabPanel.repaint(50l);
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }
}