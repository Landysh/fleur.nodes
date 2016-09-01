package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.Container;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import sun.awt.windows.WEmbeddedFrame;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * @author 
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

	private static final String NO_COLUMNS_AVAILABLE_WARNING = "No Data Available.";
	private static final String DEFAULT_SAMPLE = "Overview";

	GatingModelNodeSettings m_Settings;
    
	JPanel m_analyisTab;
	private JPanel m_analysisArea;

	JComboBox<String> fcsColumnBox;
	JComboBox<String> selectSampleBox;
	private JSpinner sampleSizeSpinner;
	private JPanel analysisPanel;
	LineageAnalysisPanel lineagePanel;

	
	protected CreateGatesNodeDialog() {
        super();
        m_Settings = new GatingModelNodeSettings();
                
        //Main analysis Tab
        m_analyisTab = new JPanel();
        m_analyisTab.setLayout(new BoxLayout(m_analyisTab, BoxLayout.Y_AXIS));
        m_analyisTab.add(Box.createVerticalGlue());
        m_analyisTab.add(createOptionsPanel());
        m_analysisArea = createAnalysisArea();
        m_analyisTab.add(m_analysisArea);

        super.addTab("Analysis", m_analyisTab);
    }
    
	private JPanel createOptionsPanel() {
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Sample Options"));
		optionsPanel.add(Box.createVerticalGlue());
		optionsPanel.add(Box.createHorizontalGlue());
		
		//Select Input data
		fcsColumnBox = new JComboBox<String>(new String[] {NO_COLUMNS_AVAILABLE_WARNING});
		fcsColumnBox.setSelectedIndex(0);
		fcsColumnBox.addActionListener(new FCSColumnBoxListener(this));
		optionsPanel.add(fcsColumnBox);
		
		//Select file
		selectSampleBox = new JComboBox<String>(new String[] {DEFAULT_SAMPLE});
		selectSampleBox.setSelectedIndex(0);
		selectSampleBox.addActionListener(new SampleBoxListener(this));
		optionsPanel.add(selectSampleBox);
		
		//Select Sample Size
		SpinnerNumberModel spinModel = new SpinnerNumberModel(10000, 0, 5000000, 1000);
		sampleSizeSpinner = new JSpinner(spinModel);
		sampleSizeSpinner.getModel().setValue(10000);
		sampleSizeSpinner.setName("Sample Size");
		optionsPanel.add(sampleSizeSpinner);

		return optionsPanel;
	}

	private JPanel createAnalysisArea() {
        JButton addChartButton = new JButton("New plot.");
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

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
            final BufferedDataTable[] input) throws NotConfigurableException {
		DataTableSpec spec = input[0].getSpec();

		//Update selected column Combo box
		fcsColumnBox.removeAllItems();
		for (String name:spec.getColumnNames()){
			if (spec.getColumnSpec(name).getType()==ColumnStoreCell.TYPE){
				fcsColumnBox.addItem(name);
			}
		}
		if (fcsColumnBox.getModel().getSize()==0){
			fcsColumnBox.addItem(NO_COLUMNS_AVAILABLE_WARNING);
		}
		
		//Update Sample List
		String targetColumn = m_Settings.getSelectedColumn();
		String[] names = input[0].getSpec().getColumnNames();
		int index = -1;
		for (int i=0;i<names.length;i++){
			if (names[i].matches(targetColumn)){
				index = i;
			}
		}	
		if(index ==-1){
			throw new NotConfigurableException("target column not in column list");
		}
		
		//read the sample names;
		BufferedDataTable table = input[0];
		selectSampleBox.removeAllItems();
		selectSampleBox.addItem(DEFAULT_SAMPLE);

		HashSet <String> set = new HashSet <String>();

		//convert it back to array.    
		if (input[0].size()>0){
			for (DataRow row:table){
				ColumnStoreCell cell = (ColumnStoreCell) row.getCell(index);
				selectSampleBox.addItem(cell.getColumnStore().getKeywordValue("$FIL"));
				List<String> newParameters = new ArrayList<String>(Arrays.asList(cell.getColumnStore().getColumnNames()));
				set.addAll(newParameters);
			}
		}
		m_Settings.setParameterList(set.toArray(new String[set.size()]));   
		if (selectSampleBox.getModel().getSize()==1){
			selectSampleBox.removeAllItems();
			selectSampleBox.addItem("No Data Available!");
		}
		
		//Load sample names.
		
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		m_Settings.save(settings);
	}

	public void addPlot() {
		 // figure out the parent to be able to make the dialog modal
		
		//This would be nicer...
		WEmbeddedFrame topFrame = (WEmbeddedFrame) SwingUtilities.getWindowAncestor(getPanel());
        
        Frame f = null;
        Container c = getPanel().getParent();
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame)c;
                break;
            }
            c = c.getParent();
        }

		AddPlotDialog dialog = new AddPlotDialog(topFrame, m_Settings);
        dialog.setVisible(true);

        if (dialog.isOK) {
        	dialog.save();
            updateLineagePanel();
        }
        dialog.dispose();
	}

	private void updateLineagePanel() {
		// TODO Create Progress Bar
		
		// TODO Create plots with swing worker
		
		// TODO Make plot visible once ready.
	}
}
//EOF