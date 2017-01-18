package main.java.inflor.knime.nodes.gating;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import main.java.inflor.core.data.DomainObject;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.gates.GateUtilities;
import main.java.inflor.core.gates.Hierarchical;
import main.java.inflor.core.ui.CellLineageTree;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

  private static final String MSG_UNABLE_TO_SAVE_NODE_SETTINGS = "Unable to save node settings.";

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";
 
  private final NodeLogger logger = getLogger();

  CreateGatesNodeSettings mSettings;

  JPanel analyisTab;
  CellLineageTree lineageTree;
  JComboBox<String> fcsColumnBox;
  JComboBox<FCSFrame> selectSampleBox;

  private JScrollPane analysisArea;

  private LineageTreeMouseAdapter ltml;

  protected CreateGatesNodeDialog() {
    super();
    mSettings = new CreateGatesNodeSettings();

    // Main analysis Tab
    analyisTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    analyisTab.setLayout(borderLayout);
    analyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);

    final JButton deleteChartButton = new JButton("Delete");
    deleteChartButton.addActionListener(e -> {
        
      if (lineageTree.getSelectionCount() == 1) {
          DefaultMutableTreeNode selectedNode =
              (DefaultMutableTreeNode) lineageTree.getSelectionPath().getLastPathComponent();
          if (!selectedNode.equals(selectedNode.getRoot()) &&
              selectedNode.getUserObject() instanceof DomainObject){
            Hierarchical node = (Hierarchical) selectedNode.getUserObject();
            mSettings.removeNode(node);
          }
        }
      }
    );

    JPanel plotButtons = new JPanel(new FlowLayout());

    plotButtons.add(deleteChartButton);
    analyisTab.add(plotButtons, BorderLayout.PAGE_END);
    super.addTab("Analysis", analyisTab);
  }

  private JScrollPane createAnalysisArea() {
    FCSFrame dataFrame = (FCSFrame) selectSampleBox.getSelectedItem();
    List<Hierarchical> nodePool = mSettings.findNodes(dataFrame.getID());
    lineageTree = new CellLineageTree(dataFrame, nodePool);
    lineageTree.removeMouseListener(ltml);
    ltml = new LineageTreeMouseAdapter(this);
    lineageTree.addMouseListener(new LineageTreeMouseAdapter(this));
    analysisArea = new JScrollPane(lineageTree);
    return analysisArea;
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
    fcsColumnBox.addActionListener(e -> {
        String columnName = (String) fcsColumnBox.getModel().getSelectedItem();
        mSettings.setSelectedColumn(columnName);
      });
    optionsPanel.add(fcsColumnBox);

    // Select file
    selectSampleBox = new JComboBox<>();
    selectSampleBox.setSelectedIndex(-1);
    selectSampleBox.addActionListener(e -> updateLineageTree());
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

    final DataTableSpec[] specs = {input[0].getSpec()};

    loadSettingsFrom(settings, specs);


    // Update Sample List
    final String targetColumn = mSettings.getSelectedColumn();
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

    // read the sample names
    final BufferedDataTable table = input[0];
    selectSampleBox.removeAllItems();

    // Hold on to a reference of the data so we can plot it later.

    final HashSet<String> parameterSet = new HashSet<>();
    
    ArrayList<FCSFrame> dataSet = new ArrayList<>();
    
    for (DataRow row : table) {
      FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) row.getCell(fcsColumnIndex)).getFCSFrameValue();
      dataSet.add(dataFrame);
      List<String> newParameters =dataFrame.getDimensionNames();
      parameterSet.addAll(newParameters);
    }
    FCSFrame concatenatedFrame = FCSUtilities.createConcatenatedFrame(dataSet);
    concatenatedFrame.setID(GateUtilities.SUMMARY_FRAME_ID);
    selectSampleBox.addItem(concatenatedFrame);
    dataSet.forEach(selectSampleBox::addItem);
    selectSampleBox.setSelectedIndex(0);
    updateLineageTree();
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    try {
      mSettings.save(settings);
    } catch (IOException e) {
      logger.error(MSG_UNABLE_TO_SAVE_NODE_SETTINGS, e);
      throw new InvalidSettingsException(MSG_UNABLE_TO_SAVE_NODE_SETTINGS);
    }
  }

  public FCSFrame getSelectedSample() {
    return (FCSFrame) selectSampleBox.getSelectedItem();
  }

  public void updateLineageTree() {
    if (analysisArea != null) {
      analyisTab.remove(analysisArea);
    }
    analysisArea = createAnalysisArea();
    analyisTab.add(analysisArea, BorderLayout.CENTER);
    analyisTab.revalidate();
    analyisTab.repaint(50);
  }
}
