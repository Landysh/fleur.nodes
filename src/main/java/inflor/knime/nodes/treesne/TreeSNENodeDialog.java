package inflor.knime.nodes.treesne;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
  private static final String LABEL_DIMENSION_BOX = "Frame Dimensions";
  private static final String LABEL_ITERATIONS = "Maximum Iterations";
  private static final String LABEL_PCA_DIMENSIONS = "Initial PCA Dimension Count";
  private static final String LABEL_PERPLEXITY = "Perplexity";
  private static final String LABEL_OBSERVATIONS = "Max Observations";

  TreeSNENodeSettings mSettings = new TreeSNENodeSettings();
  //Data Settings
  JPanel dataSettingsTab;
  JComboBox<String> columnBox = new JComboBox<>();
  private boolean isColumnListening = true;
  
  //TSNE Settings
  JPanel tSNETab;
  JSpinner maxIterationsSpinner = new JSpinner();
  JSpinner pcaDimCountSpinner = new JSpinner();
  JSpinner perplexitySpinner = new JSpinner();
  JSpinner maxObservationsSpinner = new JSpinner();

  DefaultListModel<String> dimensionModel = new DefaultListModel<>();
  JList<String> dimensionList = new JList<>(dimensionModel);
  private boolean isDimensionListening;

  //TreeSNE Settings
  JPanel treeSNETab;
  private DataTableSpec spec;
  private String[] displayNames;
  private String[] shortNames;

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

  private JPanel createDataPanel() {
    BorderLayout layout = new BorderLayout();
    JPanel panel = new JPanel(layout);
    panel.add(columnBox, BorderLayout.NORTH);
    Border dimensionTitle = BorderFactory.createTitledBorder(LABEL_DIMENSION_BOX);
    dimensionList.setBorder(dimensionTitle);
    panel.add(dimensionList, BorderLayout.CENTER);
    columnBox.addActionListener(e-> selectedColumnChanged());
    dimensionList.addListSelectionListener(e-> updateSelectedDimensions());
    return panel;
  }

  private JPanel createTreeSNETab() {
    JPanel panel = new JPanel();
    return panel;
  }

  private JPanel createTSNETab() {
    Dimension size = new Dimension(100,20);

    maxObservationsSpinner.getModel().addChangeListener(e-> mSettings.setMaxIterations((Integer) maxIterationsSpinner.getValue()));   
    maxObservationsSpinner.setPreferredSize(size);

    maxIterationsSpinner.getModel().addChangeListener(e-> mSettings.setMaxIterations((Integer) maxIterationsSpinner.getValue()));   
    maxIterationsSpinner.setPreferredSize(size);
    
    pcaDimCountSpinner.getModel().addChangeListener(e-> mSettings.setPCADims((Integer) pcaDimCountSpinner.getValue()));
    pcaDimCountSpinner.setPreferredSize(size);
    
    perplexitySpinner.getModel().addChangeListener(e-> mSettings.setPerplexity((Integer) perplexitySpinner.getValue()));
    perplexitySpinner.setPreferredSize(size);
    //Layout
    GridBagConstraints gbc = new GridBagConstraints();
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Algorithm Details"));
    gbc.gridx = 0;
    gbc.gridy = 0;
    JLabel iterLabel = new JLabel(LABEL_ITERATIONS);
    iterLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    panel.add(iterLabel, gbc);
    gbc.gridx =1;
    panel.add(maxIterationsSpinner, gbc);
    gbc.gridx=0;
    gbc.gridy=1;
    JLabel pcaLabel = new JLabel(LABEL_PCA_DIMENSIONS);
    pcaLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    panel.add(pcaLabel, gbc);
    gbc.gridx=1;
    panel.add(pcaDimCountSpinner, gbc);
    gbc.gridx=0;
    gbc.gridy=2;
    JLabel perpLabel = new JLabel(LABEL_PERPLEXITY);
    pcaLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    panel.add(perpLabel, gbc);    
    gbc.gridx = 1;
    panel.add(perplexitySpinner, gbc);
    gbc.gridx=0;
    gbc.gridy=3;
    JLabel obsLabel = new JLabel(LABEL_OBSERVATIONS);
    obsLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    panel.add(obsLabel, gbc);   
    gbc.gridx = 1;
    panel.add(maxObservationsSpinner, gbc);
    
    return panel;
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
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs){
    spec = specs[0];
    try {
      mSettings.load(settings);
      //FCSSelector
      isColumnListening = false;
      possibleColumnsChanged();
      isColumnListening = true;
      
      maxObservationsSpinner.getModel().setValue(mSettings.getMaxObservations());
      maxIterationsSpinner.getModel().setValue(mSettings.getMaxIterations());
      pcaDimCountSpinner.getModel().setValue(mSettings.getPCADims());
      perplexitySpinner.getModel().setValue(mSettings.getPerplexity());

      String columnName = mSettings.getSelectedColumn();
      if (columnName.equals(mSettings.DEFAULT_COLUMN_SELECTION)){
        columnBox.setSelectedIndex(0);
      } else {
        isDimensionListening = false;
        columnBox.setSelectedItem(columnName);
        isDimensionListening = true;
      }
      
      isDimensionListening = false;
      possibleDimenseionsChanged();
      isDimensionListening = true;

      //Set the selection state from settings if possible.
      String[] selectedDimensions = mSettings.getSelectedDimension();
      if (selectedDimensions[0].equals(mSettings.DEFAULT_SELECTED_DIMENSIONS[0])){
        
      } else {
        int[] selection = new int[selectedDimensions.length];
        for (int i=0;i<selectedDimensions.length;i++){
          for (int j=0;j<shortNames.length;j++){
            if (selectedDimensions[i].equals(shortNames[j])){
              selection[i]=j;
            }
          }
        }
        dimensionList.setSelectedIndices(selection);
      }
    } catch (InvalidSettingsException e) {
      getLogger().error("Unable to load settings.", e);
    }
  }

  private void possibleColumnsChanged() {
    List<String> possibleFCSColumns = findFCSColumns(spec);
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    possibleFCSColumns.forEach(value -> model.addElement(value));
    columnBox.setModel(model);
  }

  private void possibleDimenseionsChanged() {
    dimensionModel.removeAllElements(); 
    readDimensionInformation(spec);
    Arrays.asList(displayNames).forEach(name -> dimensionModel.addElement(name));
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

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    mSettings.save(settings);
  } 

  private void selectedColumnChanged(){
    if (isColumnListening){
      mSettings.setSelectedColumn((String) columnBox.getSelectedItem());
      possibleDimenseionsChanged();
    }
  }

  private void updateSelectedDimensions() {
    if (isDimensionListening){
      int[] selection = dimensionList.getSelectedIndices();
      String[] selectedDimesions = new String[selection.length];
      for (int i=0;i<selectedDimesions.length;i++){
        selectedDimesions[i] = shortNames[selection[i]];
      }
      mSettings.setSelectedDimension(selectedDimesions);
    }
  }
}