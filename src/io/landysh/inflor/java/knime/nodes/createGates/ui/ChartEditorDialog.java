package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.AbstractFCSPlot;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.plots.PlotUtils;
import io.landysh.inflor.java.core.transforms.AbstractDisplayTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.TransformType;
import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;
import sun.awt.windows.WEmbeddedFrame;

public class ChartEditorDialog extends JDialog {

	/**
	 * The modal dialog from which new chart definitions will be created 
	 * and existing charts may be edited
	 */

	private static final long serialVersionUID = 3249082301592821578L;
	// private static final Frame parent;
	protected JPanel previewPanel;
	protected JPanel settingsPanel;
	protected JPanel contentPanel;

	ChartSpec spec;

	private JButton m_okButton = null;
	private JButton m_cancelButton = null;
	public boolean isOK = false;
	private JComboBox<String> parentSelectorBox;
	private JComboBox<PlotTypes> plotTypeSelectorBox;
	private JPanel domainAxisGroup;
	private JComboBox<String> domainParameterBox;
	private JPanel rangeAxisGroup;
	private JComboBox<String> rangeParameterBox;
	private JProgressBar progressBar;
	private CreateGatesNodeDialog parentDialog;
	private AbstractFCSPlot previewPlot;
	private ChartPanel chartPanel;
	private JComboBox<TransformType> horizontalTransformBox;
	private JComboBox<TransformType> rangeTransformBox;


	public ChartEditorDialog(Frame topFrame, CreateGatesNodeDialog parent) {
		
		/**
		 * Use this constructor to create a new chart. 
		 * 
		 * @param topFrame the frame in which this dialog resides.  Required to make dialog modal
		 * @param parent the parent dialog which stores the data model.
		 * @param id The UUID of the domain object. typically found in the settingsModel.
		 */
		// Initialize
		super(topFrame);
		parentDialog = parent;
		spec = new ChartSpec();
		spec.getPlotType();
		spec.setDomainAxisName(parent.getSettings().getParameterList()[0]);
		spec.setRangeAxisName(parent.getSettings().getParameterList()[1]);
		spec.setDomainTransform(new BoundDisplayTransform(0, 262144));
		spec.setRangeTransform(new BoundDisplayTransform(0, 262144));
		setModal(true);

		// populate the dialog
		setTitle("Add a new plot.");
		final JPanel content = createContentPanel();
		getContentPane().add(content);
		pack();
		setLocationRelativeTo(getParent());
	}

	public ChartEditorDialog(WEmbeddedFrame topFrame, CreateGatesNodeDialog parent, String id) {
		/**
		 * Use this constructor to edit an existing chart. 
		 * 
		 * @param topFrame the frame in which this dialog resides.  Required to make dialog modal
		 * @param parent the parent dialog which stores the data model.
		 * @param id The UUID of the domain object. typically found in the settingsModel.
		 */
		super(topFrame);
		parentDialog = parent;
		spec = parentDialog.m_Settings.getChartSpec(id);
		setModal(true);

		// populate the dialog
		setTitle("Editing: " + spec.getPrefferedName());
		final JPanel content = createContentPanel();
		parentSelectorBox.setSelectedItem(spec.getParent());
		plotTypeSelectorBox.setSelectedItem(spec.getPlotType());
		domainParameterBox.setSelectedItem(spec.getDomainAxisName());
		horizontalTransformBox.setSelectedItem(spec.getDomainTransform());
		rangeParameterBox.setSelectedItem(spec.getRangeAxisName());
		rangeTransformBox.setSelectedItem(spec.getRangeTransform());

		getContentPane().add(content);
		pack();
		setLocationRelativeTo(getParent());
	}

	private JButton createCancelButton() {
		m_cancelButton = new JButton();
		m_cancelButton.setText("Cancel");
		m_cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		return m_cancelButton;
	}

	private JPanel createContentPanel() {
		// Create the panel
		progressBar = new JProgressBar();
		final Component plotOptionsPanel = createPlotOptionsPanel();
		
		contentPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		//Preview Planel
		gbc.gridx = 0;
		gbc.gridy = 0;
		previewPanel = createPreviewPanel();
		contentPanel.add(previewPanel, gbc);

		//Plot Options
		gbc.gridy = 1;
		contentPanel.add(plotOptionsPanel, gbc);
		gbc.gridy = 2;
		contentPanel.add(createHorizontalAxisGroup(), gbc);
		gbc.gridy = 3;
		contentPanel.add(createVerticalAxisGroup(), gbc);
		
		//Button Panel
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridy = 4;
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		m_okButton = createOkButton();
		m_cancelButton = createCancelButton();
		buttonPanel.add(m_okButton);
		buttonPanel.add(m_cancelButton);
		
		//ProgressBar
		gbc.gridy = 6;
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		contentPanel.add(progressBar, gbc);
		
		contentPanel.add(buttonPanel, gbc);
		contentPanel.setPreferredSize(new Dimension(300, 450));
		
		return contentPanel;
	}

	private ChartPanel createPreviewPanel() {
		ColumnStore cStore = (ColumnStore) parentDialog.getSelectedSample();
		double[] xData = cStore.getColumn(spec.getDomainAxisName());
		double[] yData = cStore.getColumn(spec.getRangeAxisName());		
		previewPlot = PlotUtils.createPlot(spec);
		JFreeChart chart = previewPlot.createChart(xData, yData);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300,250));
		return chartPanel;
	}


	private Component createHorizontalAxisGroup() {
		domainAxisGroup = new JPanel();
		domainAxisGroup.setLayout(new FlowLayout());
		domainAxisGroup
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Horizontal Axis"));
		//Parameter selector
		final String[] domainParameters = getParameterList();
		domainParameterBox = new JComboBox<String>(domainParameters);
		domainParameterBox.setSelectedIndex(guessHorizontalValueIndex(domainParameters));
		domainParameterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				spec.setDomainAxisName((String) domainParameterBox.getModel().getSelectedItem());
				updatePreviewPlot();
			}
		});
		
		//Transform selector
		final TransformType[] domainTransforms = TransformType.values();
		horizontalTransformBox = new JComboBox<TransformType>(domainTransforms);
		horizontalTransformBox.setSelectedItem(spec.getDomainTransform());
		horizontalTransformBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				TransformType selectedType = (TransformType) horizontalTransformBox.getSelectedItem();
				AbstractDisplayTransform newTransform = PlotUtils.createDefaultTransform(selectedType);
				spec.setDomainTransform(newTransform);
				updatePreviewPlot();
			}
		});

		
		domainAxisGroup.add(domainParameterBox);
		domainAxisGroup.add(horizontalTransformBox);

		return domainAxisGroup;
	}

	/**
	 * This method initializes okButton.
	 *
	 * @return javax.swing.JButton
	 */
	private JButton createOkButton() {
		m_okButton = new JButton();
		m_okButton.setText("Ok");
		m_okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updatePreviewPlot();
				isOK = true;
				setVisible(false);
			}
		});
		return m_okButton;
	}

	private JComboBox<String> createParentSelector() {
		parentSelectorBox = new JComboBox<String>(new String[] {});
		parentSelectorBox.setSelectedIndex(-1);
		parentSelectorBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				//ColumnStore parent = (ColumnStore) parentSelectorBox.getModel().getSelectedItem();
				//TODO: Figure out how to reference other populations.
				spec.setParent(null);
				updatePreviewPlot();
			}
		});
		return parentSelectorBox;
	}

	protected void updatePreviewPlot() {
		progressBar.setVisible(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Initializing");
		progressBar.getModel().setValue(1);
		ColumnStore data = (ColumnStore) parentDialog.getSelectedSample();
		double[] xData = data.getColumn(spec.getDomainAxisName());
		double[] yData = data.getColumn(spec.getRangeAxisName());		
		UpdatePlotWorker worker = new UpdatePlotWorker(progressBar, chartPanel, spec, xData, yData);
		worker.execute();
	}

	private JPanel createPlotOptionsPanel() {
		final JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "General Options"));
		parentSelectorBox = createParentSelector();
		panel.add(parentSelectorBox);
		plotTypeSelectorBox = createPlotTypeSelector();
		panel.add(plotTypeSelectorBox);
		return panel;
	}

	private JComboBox<PlotTypes> createPlotTypeSelector() {
		plotTypeSelectorBox = new JComboBox<PlotTypes>(PlotTypes.values());
		plotTypeSelectorBox.setSelectedItem(spec.getPlotType());
		plotTypeSelectorBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				PlotTypes newValue = (PlotTypes) plotTypeSelectorBox.getModel().getSelectedItem();
				spec.setPlotType(newValue);
				updatePreviewPlot();
			}
		});
		return plotTypeSelectorBox;
	}

	private Component createVerticalAxisGroup() {
		rangeAxisGroup = new JPanel();
		rangeAxisGroup.setLayout(new FlowLayout());
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vertical Axis");
		rangeAxisGroup.setBorder(border);
		
		
		final String[] rangeParameters = getParameterList();
		rangeParameterBox = new JComboBox<String>(rangeParameters);
		rangeParameterBox.setSelectedIndex(guessVerticalValueIndex(rangeParameters));
		spec.setRangeAxisName((String) rangeParameterBox.getModel().getSelectedItem());
		rangeParameterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				spec.setRangeAxisName((String) rangeParameterBox.getModel().getSelectedItem());
				updatePreviewPlot();
			}
		});
		
		//Transform selector
		final TransformType[] domainTransforms = TransformType.values();
		rangeTransformBox = new JComboBox<TransformType>(domainTransforms);
		rangeTransformBox.setSelectedItem(spec.getRangeTransform());
		rangeTransformBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				TransformType selectedType = (TransformType) rangeTransformBox.getSelectedItem();
				AbstractDisplayTransform newTransform = PlotUtils.createDefaultTransform(selectedType);
				spec.setRangeTransform(newTransform);
				updatePreviewPlot();
			}
		});
		
		//Add the components
		rangeAxisGroup.add(rangeParameterBox);
		rangeAxisGroup.add(rangeTransformBox);
		
		return rangeAxisGroup;
	}

	private String[] getParameterList() {
		final ArrayList<String> options = new ArrayList<String>();
		for (final String name : parentDialog.getSettings().getParameterList()) {
			options.add(name);
		}
		return options.toArray(new String[options.size()]);
	}

	private int guessHorizontalValueIndex(String[] horizontalOptions) {
		return 0;
	}

	private int guessVerticalValueIndex(String[] verticalOptions) {
		return 1;
	}

	public ChartSpec getChartSpec() {
		return spec;
	}
}
// EOF