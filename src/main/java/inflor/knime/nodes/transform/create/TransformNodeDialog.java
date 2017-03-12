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
package inflor.knime.nodes.transform.create;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "Transform" Node.
 * 
 * @author Aaron Hart
 */

public class TransformNodeDialog extends NodeDialogPane {
  
  private static final NodeLogger logger = NodeLogger.getLogger(TransformNodeDialog.class);

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private TransformNodeSettings modelSettings;
  private JPanel analysisTab;
  private JComboBox<String> fcsColumnBox;
  private JComboBox<String> referenceSubsetBox;
  private boolean refBoxIsActive;

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
    fcsColumnBox.addActionListener( e -> 
        modelSettings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem()));
    optionsPanel.add(fcsColumnBox);
    // Select Reference Subset
    referenceSubsetBox = new JComboBox<>();
    referenceSubsetBox.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED&&refBoxIsActive){
        	String subsetName = (String) referenceSubsetBox.getModel().getSelectedItem();
        	modelSettings.setReferenceSubset(subsetName);
        }
      });
    optionsPanel.add(referenceSubsetBox);
    
    return optionsPanel;
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
   
    DataColumnProperties props = spec.getColumnSpec((String) fcsColumnBox.getSelectedItem()).getProperties();
    
    // Update reference subset box
    refBoxIsActive = false;
    referenceSubsetBox.removeAllItems();
    referenceSubsetBox.addItem(TransformNodeSettings.DEFAULT_REFERENCE_SUBSET);
    if (props.containsProperty(NodeUtilities.SUBSET_NAMES_KEY)){
      String[] subsetNames = props.getProperty(NodeUtilities.SUBSET_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
      Arrays.asList(subsetNames).forEach(referenceSubsetBox::addItem);
    }
    refBoxIsActive = true;
    referenceSubsetBox.setSelectedItem(modelSettings.getReferenceSubset()); 
  }
  
  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    modelSettings.save(settings);
  }
}