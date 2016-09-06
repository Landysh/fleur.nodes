package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Color;
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
import javax.swing.SwingWorker;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.AbstractFCSPlot;
import io.landysh.inflor.java.core.plots.ContourPlot;
import io.landysh.inflor.java.core.plots.FakePlot;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.plots.PlotUtils;
import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;
import io.landysh.inflor.java.knime.nodes.createGates.GatingModelNodeSettings;

public class AddPlotDialog extends JDialog {

	/**
	 * The modal dialog from which new plot definitions will be created.
	 */

	private static final long serialVersionUID = 3249082301592821578L;
	private static final String DEFAULT_PARENT = "UNGATED";
	private static final String DEFAULT_PLOT_TYPE = "FAKE";
	// private static final Frame parent;
	protected JPanel previewPanel;
	protected JPanel settingsPanel;
	protected JPanel contentPanel;

	PlotSpec spec;

	private final String[] populationList;
	private final String[] parameterList;

	private JButton m_okButton = null;
	private JButton m_cancelButton = null;
	public boolean isOK = false;
	private JComboBox<String> parentSelectorBox;
	private JComboBox<PlotTypes> plotTypeSelectorBox;
	private JPanel horizontalAxisGroup;
	private JComboBox<String> horizontalParameterBox;
	private JPanel verticalAxisGroup;
	private JComboBox<String> verticalParameterBox;
	private final GatingModelNodeSettings m_settings;
	private JProgressBar progressBar;
	private CreateGatesNodeDialog parentDialog;
	private AbstractFCSPlot previewPlot;


	public AddPlotDialog(Frame topFrame, CreateGatesNodeDialog parent) {
		// Initialize
		super(topFrame);
		spec = new PlotSpec(null);
		spec.setPlotType(PlotTypes.Fake);
		spec.setParent(null);
		spec.setVerticalAxis("Dummy Y");
		spec.setHorizontalAxis("Dummy X");
		m_settings = parent.m_Settings;
		parentDialog = parent;
		setModal(true);

		// populate the dialog
		populationList = m_settings.getSubsetList();
		parameterList = m_settings.getParameterList();
		setTitle("Add a new plot.");
		final JPanel content = createContentPanel();
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

		//ProgressBar
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridy = 4;
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		contentPanel.add(progressBar, gbc);
		
		//Button Panel
		gbc.gridy = 5;
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		m_okButton = createOkButton();
		m_cancelButton = createCancelButton();
		buttonPanel.add(m_okButton);
		buttonPanel.add(m_cancelButton);
		
		contentPanel.add(buttonPanel, gbc);
		contentPanel.setPreferredSize(new Dimension(300, 500));
		
		return contentPanel;
	}

	private ChartPanel createPreviewPanel() {
		ColumnStore data = (ColumnStore) parentDialog.getSelectedSample();
		double[] xData = data.getColumn(spec.getDomainAxisName());
		double[] yData = data.getColumn(spec.getRangeAxisName());		
		previewPlot = PlotUtils.createPlot(spec);
		JFreeChart chart = previewPlot.createChart(xData, yData);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(250,250));
		return panel;
		}


	private Component createHorizontalAxisGroup() {
		horizontalAxisGroup = new JPanel();
		horizontalAxisGroup.setLayout(new FlowLayout());
		final String[] horizontalOptions = getParameterList();
		horizontalAxisGroup
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Horizontal Axis"));
		horizontalParameterBox = new JComboBox<String>(horizontalOptions);
		horizontalParameterBox.setSelectedIndex(guessHorizontalValueIndex(horizontalOptions));
		horizontalParameterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				spec.setHorizontalAxis((String) horizontalParameterBox.getModel().getSelectedItem());
				updatePreviewPlot();
			}
		});
		horizontalAxisGroup.add(horizontalParameterBox);
		return horizontalAxisGroup;
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
		parentSelectorBox = new JComboBox<String>(new String[] { DEFAULT_PARENT });
		parentSelectorBox.setSelectedIndex(0);
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
		previewPanel.setBackground(Color.GRAY);
		progressBar.setVisible(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Initializing");
		progressBar.getModel().setValue(1);
		ColumnStore data = (ColumnStore) parentDialog.getSelectedSample();
		double[] xData = data.getColumn(spec.getDomainAxisName());
		double[] yData = data.getColumn(spec.getRangeAxisName());		
		UpdatePlotWorker worker = new UpdatePlotWorker(progressBar, spec, xData, yData);
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
		plotTypeSelectorBox.setSelectedIndex(0);
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
		verticalAxisGroup = new JPanel();
		verticalAxisGroup.setLayout(new FlowLayout());
		verticalAxisGroup
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Vertical Axis"));
		final String[] verticalOptions = getParameterList();
		verticalParameterBox = new JComboBox<String>(verticalOptions);
		verticalParameterBox.setSelectedIndex(guessVerticalValueIndex(verticalOptions));
		verticalParameterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				spec.setVerticalAxis((String) horizontalParameterBox.getModel().getSelectedItem());
				updatePreviewPlot();
			}
		});
		verticalAxisGroup.add(verticalParameterBox);
		return verticalAxisGroup;
	}

	private String[] getParameterList() {
		final ArrayList<String> options = new ArrayList<String>();
		for (final String name : parameterList) {
			options.add(name);
		}
		options.add("Rank");
		return options.toArray(new String[options.size()]);
	}

	private int guessHorizontalValueIndex(String[] horizontalOptions) {
		// TODO guess better.
		return 0;
	}

	private int guessVerticalValueIndex(String[] verticalOptions) {
		// TODO guess better.
		return 1;
	}

	public void save() {
		m_settings.addPlotSpec(spec);
	}
}
// EOF