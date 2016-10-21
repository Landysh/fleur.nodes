package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.nodes.createGates.ui.CellLineageTree;
import io.landysh.inflor.java.knime.nodes.createGates.ui.ChartEditorDialog;
import sun.awt.windows.WEmbeddedFrame;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

	private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

	public GatingModelNodeSettings m_Settings;

	JPanel m_analyisTab;
	CellLineageTree lineageTree;
	public JComboBox<String> fcsColumnBox;
	public JComboBox<ColumnStore> selectSampleBox;

	protected CreateGatesNodeDialog() {
		super();
		m_Settings = new GatingModelNodeSettings();

		// Main analysis Tab
		m_analyisTab = new JPanel();
		BorderLayout borderLayout = new BorderLayout();
		m_analyisTab.setLayout(borderLayout);
		m_analyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);
		
		final JButton addChartButton = new JButton("Add");
		addChartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addChartDialog();
			}
		});

		final JButton editChartButton = new JButton("Edit");
		editChartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (lineageTree.getSelectionCount()==1){
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) lineageTree.getSelectionPath().getLastPathComponent();
					String id = ((ChartSpec) selectedNode.getUserObject()).ID;
					editChartDialog(id);
					updateLineageTree();
				}
			}
		});
		
		final JButton deleteChartButton = new JButton("Delete");
		deleteChartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (lineageTree.getSelectionCount()==1){
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) lineageTree.getSelectionPath().getLastPathComponent();
					String id = ((ChartSpec) selectedNode.getUserObject()).ID;
					m_Settings.deleteChart(id);
					updateLineageTree();
				}
			}
		});
		
		JPanel plotButtons = new JPanel(new FlowLayout());
		
		plotButtons.add(addChartButton);
		plotButtons.add(editChartButton);
		plotButtons.add(deleteChartButton);

		m_analyisTab.add(plotButtons, BorderLayout.PAGE_END);
		
		super.addTab("Analysis", m_analyisTab);
	}

	public void addChartDialog() {
		// figure out the parent to be able to make the dialog modal
		final WEmbeddedFrame topFrame = (WEmbeddedFrame) SwingUtilities.getWindowAncestor(getPanel());

		final ChartEditorDialog dialog = new ChartEditorDialog(topFrame, this);
 		dialog.setVisible(true);

		if (dialog.isOK) {
			m_Settings.addPlotSpec(dialog.getChartSpec());
			updateLineageTree();
		}
		dialog.dispose();
	}
	
	public void editChartDialog(String id) {
		// figure out the parent to be able to make the dialog modal
		final WEmbeddedFrame topFrame = (WEmbeddedFrame) SwingUtilities.getWindowAncestor(getPanel());

		final ChartEditorDialog dialog = new ChartEditorDialog(topFrame, this, id);
 		dialog.setVisible(true);

		if (dialog.isOK) {
			m_Settings.addPlotSpec(dialog.getChartSpec());
			updateLineageTree();
		}
		dialog.dispose();
	}

	private CellLineageTree createAnalysisArea() {
		
		ColumnStore dataStore = (ColumnStore) selectSampleBox.getSelectedItem();
		Hashtable<String, ChartSpec> chartSpecs = m_Settings.getPlotSpecs();
		
		lineageTree = new CellLineageTree();
		lineageTree.updateLayout(chartSpecs, dataStore);

		return lineageTree;
	}

	private JPanel createOptionsPanel() {
		final JPanel optionsPanel = new JPanel();
		
		FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
		
		optionsPanel.setLayout(layout);
		optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sample Options"));
		optionsPanel.add(Box.createVerticalGlue());
		optionsPanel.add(Box.createHorizontalGlue());

		// Select Input data
		fcsColumnBox = new JComboBox<String>(new String[] { NO_COLUMNS_AVAILABLE_WARNING });
		fcsColumnBox.setSelectedIndex(0);
		fcsColumnBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_Settings.setSelectedColumn((String) fcsColumnBox.getModel().getSelectedItem());
			}
		});
		optionsPanel.add(fcsColumnBox);
		
		// Select file
		selectSampleBox = new JComboBox<ColumnStore>();
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
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		final DataTableSpec spec = specs[0];

		// Update selected column Combo box
		fcsColumnBox.removeAllItems();
		for (final String name : spec.getColumnNames()) {
			if (spec.getColumnSpec(name).getType() == ColumnStoreCell.TYPE) {
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
		final String targetColumn = m_Settings.getSelectedColumn();
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
		
		//Hold on to a reference of the data so we can plot it later.
		
		final HashSet<String> parameterSet = new HashSet<String>();
		
		for (final DataRow row : table) {
			final ColumnStore cStoreData = ((ColumnStoreCell) row.getCell(fcsColumnIndex)).getColumnStore();
			selectSampleBox.addItem(cStoreData);
			final List<String> newParameters = new ArrayList<String>(
					Arrays.asList(cStoreData.getColumnNames()));
			parameterSet.addAll(newParameters);
		}
		if (selectSampleBox.getModel().getSize() == 0) {
			selectSampleBox.removeAllItems();
			selectSampleBox.setEnabled(false);
			selectSampleBox.setToolTipText("No FCS Files found");
		}
		updateLineageTree();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		try {
			m_Settings.save(settings);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Unable to save node settings.");
		}
	}

	public ColumnStore getSelectedSample() {
		return (ColumnStore)this.selectSampleBox.getSelectedItem();
	}
	
	private void updateLineageTree(){
		if (lineageTree!=null){
			m_analyisTab.remove(lineageTree);
		}
		lineageTree = createAnalysisArea();
		m_analyisTab.add(lineageTree, BorderLayout.CENTER);
		m_analyisTab.revalidate();//TODO: needed?
		m_analyisTab.repaint(50L);//TODO: needed?
	}

	public GatingModelNodeSettings getSettings() {
		return m_Settings;
	}
}
