package inflor.knime.nodes.treesne;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ListSelectionEvent;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import inflor.knime.core.NodeUtilities;
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
  
  //TSNE Settings
  JPanel tSNETab;
  JSpinner maxIterationsSpinner = new JSpinner();
  JSpinner pcaDimCountSpinner = new JSpinner();
  JSpinner perplexitySpinner = new JSpinner();
  DefaultListModel<String> dimensionModel = new DefaultListModel<>();
  JList<String> dimensionList = new JList<>(dimensionModel);
  
  //TreeSNE Settings
  JPanel treeSNETab;
  private DataTableSpec spec;
  private String[] displayNames;
  private String[] shortNames;
  private boolean isDimensionListening;

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
    BorderLayout layout = new BorderLayout();
    JPanel panel = new JPanel(layout);
    panel.add(columnBox, BorderLayout.NORTH);
    panel.add(dimensionList, BorderLayout.CENTER);
    
    dimensionList.addListSelectionListener(e-> updateSelectedDimensions(e));
    
    return panel;
  }
  
  private void updateSelectedDimensions(ListSelectionEvent e) {
    if (isDimensionListening){
      int[] selection = dimensionList.getSelectedIndices();
      String[] selectedDimesions = new String[selection.length];
      for (int i=0;i<selectedDimesions.length;i++){
        selectedDimesions[i] = shortNames[selection[i]];
      }
      mSettings.setSelectedDimension(selectedDimesions);
    }
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
      
      updateDimensions(null);
      
      //Set the selection state from settings if possible.
      String[] selectedDimensions = mSettings.getSelectedDimension();
      int[] selection = new int[selectedDimensions.length];
      for (int i=0;i<selectedDimensions.length;i++){
        for (int j=0;j<shortNames.length;j++){
          if (selectedDimensions[i].equals(shortNames[j])){
            selection[i]=j;
          }
        }
      }
      dimensionList.setSelectedIndices(selection);
      
      
    } catch (InvalidSettingsException e) {
      getLogger().error("Unable to load settings.", e);
    }
  }

  private void updateFCSColumn(ActionEvent e){
    mSettings.setSelectedColumn((String) columnBox.getSelectedItem());
    updateDimensions(e);
  }
  
  private void updateDimensions(ActionEvent e) {
    isDimensionListening = false;
    dimensionModel.removeAllElements(); 
    readDimensionInformation(spec);
    Arrays.asList(displayNames).forEach(name -> dimensionModel.addElement(name));
    isDimensionListening = true;
  }

  private void readDimensionInformation(DataTableSpec spec) {
    DataColumnSpec colSpec = spec.getColumnSpec(mSettings.getSelectedColumn());
    DataColumnProperties props = colSpec.getProperties();
    boolean hasDisplayNames = props.containsProperty(NodeUtilities.DISPLAY_NAMES_KEY);
    boolean hasShortNames = props.containsProperty(NodeUtilities.DIMENSION_NAMES_KEY);
    
    if (hasDisplayNames&&hasShortNames){
      shortNames = props.getProperty(NodeUtilities.DIMENSION_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
      displayNames = props.getProperty(NodeUtilities.DISPLAY_NAMES_KEY).split(NodeUtilities.DELIMITER_REGEX);
    } else {
      throw new RuntimeException("Unable to parse dimension names");
    }

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

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    mSettings.save(settings);
  }
}