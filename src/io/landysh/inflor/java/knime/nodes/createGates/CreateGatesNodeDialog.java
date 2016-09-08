package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.InflorVisDataSet;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.nodes.createGates.ui.AddPlotActionListener;
import io.landysh.inflor.java.knime.nodes.createGates.ui.AddPlotDialog;
import io.landysh.inflor.java.knime.nodes.createGates.ui.FCSColumnBoxListener;
import io.landysh.inflor.java.knime.nodes.createGates.ui.LineageAnalysisPanel;
import sun.awt.windows.WEmbeddedFrame;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * 
 * @author
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

	private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";

	public GatingModelNodeSettings m_Settings;
	
	public InflorVisDataSet currentDataSet;

	JPanel m_analyisTab;
	private final JPanel m_analysisArea;

	public JComboBox<String> fcsColumnBox;
	public JComboBox<ColumnStore> selectSampleBox;
	//TODO: Remove this?
	//	private JSpinner sampleSizeSpinner;
	private JPanel analysisPanel;
	LineageAnalysisPanel lineagePanel;
	private Hashtable<String, ColumnStore> data;

	public Hashtable<String, ColumnStore> getData() {
		return data;
	}

	protected CreateGatesNodeDialog() {
		super();
		m_Settings = new GatingModelNodeSettings();

		// Main analysis Tab
		m_analyisTab = new JPanel();
		m_analyisTab.setLayout(new BoxLayout(m_analyisTab, BoxLayout.Y_AXIS));
		m_analyisTab.add(Box.createVerticalGlue());
		m_analyisTab.add(createOptionsPanel());
		m_analysisArea = createAnalysisArea();
		m_analyisTab.add(m_analysisArea);

		super.addTab("Analysis", m_analyisTab);
	}

	public void addPlot() {
		// figure out the parent to be able to make the dialog modal
		final WEmbeddedFrame topFrame = (WEmbeddedFrame) SwingUtilities.getWindowAncestor(getPanel());

		final AddPlotDialog dialog = new AddPlotDialog(topFrame, this);
 		dialog.setVisible(true);

		if (dialog.isOK) {
			dialog.save();
			updateLineagePanel();
		}
		dialog.dispose();
	}

	private JPanel createAnalysisArea() {
		final JButton addChartButton = new JButton("New plot.");
		addChartButton.addActionListener(new AddPlotActionListener(this));

		lineagePanel = new LineageAnalysisPanel();

		analysisPanel = new JPanel();
		analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
		analysisPanel.add(Box.createVerticalGlue());
		analysisPanel.add(Box.createHorizontalGlue());
		analysisPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Analyis"));
		analysisPanel.add(lineagePanel);
		analysisPanel.add(addChartButton);

		return analysisPanel;
	}

	private JPanel createOptionsPanel() {
		final JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sample Options"));
		optionsPanel.add(Box.createVerticalGlue());
		optionsPanel.add(Box.createHorizontalGlue());

		// Select Input data
		fcsColumnBox = new JComboBox<String>(new String[] { NO_COLUMNS_AVAILABLE_WARNING });
		fcsColumnBox.setSelectedIndex(0);
		fcsColumnBox.addActionListener(new FCSColumnBoxListener(this));
		optionsPanel.add(fcsColumnBox);

		// Select file
		selectSampleBox = new JComboBox<ColumnStore>();
		selectSampleBox.setSelectedIndex(-1);
		selectSampleBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		optionsPanel.add(selectSampleBox);

//		// Select Sample Size
//		final SpinnerNumberModel spinModel = new SpinnerNumberModel(10000, 0, 5000000, 1000);
//		sampleSizeSpinner = new JSpinner(spinModel);
//		sampleSizeSpinner.getModel().setValue(10000);
//		sampleSizeSpinner.setName("Sample Size");
//		optionsPanel.add(sampleSizeSpinner);

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
	};
	
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
		
		m_Settings.setParameterList(parameterSet.toArray(new String[parameterSet.size()]));
		
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

	private void updateLineagePanel() {
		// TODO Create Progress Bar

		// TODO Create plots with swing worker

		// TODO Make plot visible once ready.
	}

	public ColumnStore getSelectedSample() {
		return (ColumnStore)this.selectSampleBox.getSelectedItem();
	}
}
// EOF