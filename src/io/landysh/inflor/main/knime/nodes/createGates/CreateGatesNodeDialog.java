package io.landysh.inflor.main.knime.nodes.createGates;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.main.core.dataStructures.DomainObject;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.gates.GateUtilities;
import io.landysh.inflor.main.core.gates.Hierarchical;
import io.landysh.inflor.main.core.gates.ui.LineageTreeMouseAdapter;
import io.landysh.inflor.main.core.ui.CellLineageTree;
import io.landysh.inflor.main.core.utils.FCSUtilities;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameFileStoreDataCell;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

  private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

  private static final Integer DEFAULT_SUMMARY_FRAME_EVENT_COUNT = 10000;

  public CreateGatesNodeSettings m_settings;

  JPanel analyisTab;
  public CellLineageTree lineageTree;
  public JComboBox<String> fcsColumnBox;
  public JComboBox<FCSFrame> selectSampleBox;

  private ArrayList<FCSFrame> dataSet;

  private JScrollPane analysisArea;

  private LineageTreeMouseAdapter ltml;

  protected CreateGatesNodeDialog() {
    super();
    m_settings = new CreateGatesNodeSettings();

    // Main analysis Tab
    analyisTab = new JPanel();
    BorderLayout borderLayout = new BorderLayout();
    analyisTab.setLayout(borderLayout);
    analyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);

    final JButton deleteChartButton = new JButton("Delete");
    deleteChartButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (lineageTree.getSelectionCount() == 1) {
          DefaultMutableTreeNode selectedNode =
              (DefaultMutableTreeNode) lineageTree.getSelectionPath().getLastPathComponent();
          if (!selectedNode.equals(selectedNode.getRoot()) &&
              selectedNode.getUserObject() instanceof DomainObject){
            Hierarchical node = ((Hierarchical) selectedNode.getUserObject());
            m_settings.removeNode(node);
          }
        }
      }
    });

    JPanel plotButtons = new JPanel(new FlowLayout());

    plotButtons.add(deleteChartButton);
    analyisTab.add(plotButtons, BorderLayout.PAGE_END);
    super.addTab("Analysis", analyisTab);
  }

  private JScrollPane createAnalysisArea() {
    FCSFrame dataFrame = (FCSFrame) selectSampleBox.getSelectedItem();
    List<Hierarchical> nodePool = m_settings.findNodes(dataFrame.getID());
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
    fcsColumnBox = new JComboBox<String>(new String[] {NO_COLUMNS_AVAILABLE_WARNING});
    fcsColumnBox.setSelectedIndex(0);
    fcsColumnBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        m_settings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem());
      }
    });
    optionsPanel.add(fcsColumnBox);

    // Select file
    selectSampleBox = new JComboBox<FCSFrame>();
    selectSampleBox.setSelectedIndex(-1);
    selectSampleBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        updateLineageTree();
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
    final String targetColumn = m_settings.getSelectedColumn();
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
      FCSFrame dataFrame = ((FCSFrameFileStoreDataCell) row.getCell(fcsColumnIndex)).getFCSFrameValue();
      dataSet.add(dataFrame);
      List<String> newParameters =dataFrame.getColumnNames();
      parameterSet.addAll(newParameters);
    }
    FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(dataSet, DEFAULT_SUMMARY_FRAME_EVENT_COUNT);
    summaryFrame.setID(GateUtilities.SUMMARY_FRAME_ID);
    selectSampleBox.addItem(summaryFrame);
    dataSet.forEach(dataFrame -> selectSampleBox.addItem(dataFrame));
    selectSampleBox.setSelectedIndex(0);
    updateLineageTree();
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    try {
      m_settings.save(settings);
    } catch (IOException e) {
      e.printStackTrace();
      throw new InvalidSettingsException("Unable to save node settings.");
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
