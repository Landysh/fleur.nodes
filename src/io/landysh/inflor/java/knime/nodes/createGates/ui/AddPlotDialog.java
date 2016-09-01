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

import io.landysh.inflor.java.core.plots.FakePlot;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;
import io.landysh.inflor.java.knime.nodes.createGates.GatingModelNodeSettings;
import sun.awt.windows.WEmbeddedFrame;

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
	private JComboBox<String> plotTypeSelectorBox;
	private JPanel horizontalAxisGroup;
	private JComboBox<String> horizontalParameterBox;
	private JPanel verticalAxisGroup;
	private JComboBox<String> verticalParameterBox;
	private final GatingModelNodeSettings m_settings;
	private JProgressBar progressBar;
	private CreateGatesNodeDialog parentDialog;


	public AddPlotDialog(Frame topFrame, CreateGatesNodeDialog parent) {
		// Initialize
		super(topFrame);
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
		contentPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		contentPanel.setBackground(Color.RED);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		// Create children
		previewPanel = new FakePlot(null).getPanel();
		
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		m_okButton = createOkButton();
		m_cancelButton = createCancelButton();
		buttonPanel.add(m_okButton);
		buttonPanel.add(m_cancelButton);

		//Lay it all out.
		gbc.gridx = 0;
		gbc.gridy = 0;
		contentPanel.add(previewPanel, gbc);

		gbc.gridy = 1;
		final Component plotOptionsPanel = createPlotOptionsPanel();
		contentPanel.add(plotOptionsPanel, gbc);

		gbc.gridy = 2;
		contentPanel.add(createHorizontalAxisGroup(), gbc);

		gbc.gridy = 3;
		contentPanel.add(createVerticalAxisGroup(), gbc);


		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridy = 4;
		contentPanel.add(progressBar, gbc);
		
		gbc.gridy = 5;
		contentPanel.add(buttonPanel, gbc);
		contentPanel.setPreferredSize(new Dimension(400, 400));
		return contentPanel;
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
				previewPanel.setBackground(Color.GRAY);
				progressBar.setVisible(true);
				progressBar.setStringPainted(true);
				progressBar.setString("Initializing");
				progressBar.getModel().setValue(1);
				UpdatePlotWorker updatePlot = new UpdatePlotWorker(previewPanel, progressBar, spec, parentDialog.currentDataSet);
				updatePlot.execute();
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
				spec.setParent((String) parentSelectorBox.getModel().getSelectedItem());
				UpdatePlotWorker worker = new UpdatePlotWorker(previewPanel, progressBar, spec, parentDialog.currentDataSet);
			}
		});
		return parentSelectorBox;
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

	private JComboBox<String> createPlotTypeSelector() {
		plotTypeSelectorBox = new JComboBox<String>(new String[] { DEFAULT_PLOT_TYPE });
		plotTypeSelectorBox.setSelectedIndex(0);
		plotTypeSelectorBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				spec.setPlotType((String) plotTypeSelectorBox.getModel().getSelectedItem());

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
				spec.setHorizontalAxis((String) horizontalParameterBox.getModel().getSelectedItem());

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