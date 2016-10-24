package io.landysh.inflor.java.knime.nodes.compensate;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.compensation.SpilloverCompensator;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.nodes.createGates.ui.CellLineageTree;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CompensateNodeDialog extends DataAwareNodeDialogPane {

	private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

	public CompensateNodeSettings m_Settings;

	JPanel m_analyisTab;
	CellLineageTree lineageTree;
	public JComboBox<String> fcsColumnBox;
	public JComboBox<ColumnStore> selectSampleBox;

	protected CompensateNodeDialog() 
	{
		super();
		m_Settings = new CompensateNodeSettings();
		// Main analysis Tab
		m_analyisTab = new JPanel();
		BorderLayout borderLayout = new BorderLayout();
		m_analyisTab.setLayout(borderLayout);
		m_analyisTab.add(createOptionsPanel(), BorderLayout.PAGE_START);
		super.addTab("From FCS File", m_analyisTab);
	}

	private JPanel createOptionsPanel() 
	{
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
				ColumnStore fcs = (ColumnStore) selectSampleBox.getSelectedItem();
				parseSpillover(fcs);
			}


		});
		optionsPanel.add(selectSampleBox);
		return optionsPanel;
	}
	
	private void parseSpillover(ColumnStore fcs) {
		SpilloverCompensator compr = new SpilloverCompensator(fcs.getKeywords());
		m_Settings.setHeader(fcs.getKeywords());
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
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		m_Settings.save(settings);
	}

	public ColumnStore getSelectedSample() {
		return (ColumnStore)this.selectSampleBox.getSelectedItem();
	}
}
