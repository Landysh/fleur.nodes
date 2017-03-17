package inflor.knime.nodes.treesne;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;


/**
 * <code>NodeDialog</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: https://github.com/lejon/T-SNE-Java
 *
 * 
 * @author Aaron Hart
 */
public class TreeSNENodeDialog extends NodeDialogPane {
  private static final String TITLE_DATA_TAB = "Data Settings";
  private static final String TITLE_TSNE_TAB = "tSNE Settings";
  private static final String TITLE_TREESNE_TAB = "TreeSNE Settings";

  TreeSNENodeSettings mSettings = new TreeSNENodeSettings();
  //Data Settings
  JPanel dataSettingsTab;
  JComboBox<String> columnBox = new JComboBox<>();
  JComboBox<String> dimensionBox = new JComboBox<>();
  
  //TSNE Settings
  JPanel tSNETab;
  JSpinner maxIterationsSpinner = new JSpinner();
  JSpinner pcaDimCountSpinner = new JSpinner();
  JSpinner perplexitySpinner = new JSpinner();
  
  //TreeSNE Settings
  JPanel treeSNETab;
  private DataTableSpec spec;

  /**
   * New pane for configuring the TSNE node.
   */
  protected TreeSNENodeDialog() {
    super();
    // Data tab.
    dataSettingsTab = createDataPanel();
    super.addTab(TITLE_DATA_TAB, dataSettingsTab);
    // tSNE tab.
    tSNETab = createTSNETab();
    super.addTab(TITLE_TSNE_TAB, tSNETab);    
    // TreeSNE tab.
    treeSNETab = createTreeSNETab();
    super.addTab(TITLE_TREESNE_TAB, treeSNETab);    

  }

  private JPanel createTreeSNETab() {
    JPanel panel = new JPanel();
    return panel;
  }

  private JPanel createTSNETab() {
    JPanel panel = new JPanel();
    panel.add(maxIterationsSpinner);
    panel.add(pcaDimCountSpinner);
    panel.add(perplexitySpinner);
    return panel;
  }

  private JPanel createDataPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(columnBox);
    panel.add(dimensionBox);
    return panel;
  }
  
  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs){
    spec = specs[0];
    try {
      mSettings.load(settings);
      //FCSSelector
      List<String> possibleFCSColumns = findFCSColumns(spec);
      DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
      possibleFCSColumns.forEach(value -> model.addElement(value));
      columnBox.setModel(model);
      columnBox.setSelectedIndex(0);
      columnBox.addActionListener(e-> updateFCSColumn(e));
      
      List<String> possibleDimensions = findDimensionNames(spec);
      
      if (mSettings.getSelectedColumn().equals(TreeSNENodeSettings.DEFAULT_COLUMN_SELECTION)){
      }
      
      
      
      
    } catch (InvalidSettingsException e) {
      getLogger().error("Unable to load settings.", e);
    }
  }

  private void updateFCSColumn(ActionEvent e){
    mSettings.setSelectedColumn((String) columnBox.getSelectedItem());
    updateDimensions(e);
  }
  
  private void updateDimensions(ActionEvent e) {
     dimensionBox.removeAllItems();
     findDimensionNames(spec).forEach(name -> dimensionBox.addItem(name));
  }

  private List<String> findDimensionNames(DataTableSpec spec) {
    // TODO Auto-generated method stub
    return null;
  }

  private List<String> findFCSColumns(DataTableSpec dataTableSpec) {
    List<String> columnSpec = Arrays.asList(dataTableSpec.getColumnNames())
      .stream()
      .map(name -> dataTableSpec.getColumnSpec(name))
      .filter(cSpec -> cSpec.getType().equals(FCSFrameFileStoreDataCell.TYPE))
      .map(spec -> spec.getName())
      .collect(Collectors.toList());
    return columnSpec;
  }

  private void updateDataPanel() {
    // TODO Auto-generated method stub 
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    mSettings.save(settings);
  }
}