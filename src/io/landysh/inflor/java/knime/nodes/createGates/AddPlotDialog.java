package io.landysh.inflor.java.knime.nodes.createGates;

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

public class AddPlotDialog extends JDialog{
	
	/**
	 * The modal dialog from which new plot definitions will be created.
	 */
	
	private static final long serialVersionUID = 3249082301592821578L;
	private static final String DEFAULT_PARENT = "UNGATED";
	private static final String DEFAULT_PLOT_TYPE = "FAKE";
//	private static final Frame parent;
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
	private GatingModelNodeSettings m_settings;
	

	public AddPlotDialog(Frame parentFrame, GatingModelNodeSettings settings) {
        //Initialize
		super(parentFrame);
        this.m_settings = settings;
        setModal(true);
       
        //populate the dialog
        this.populationList = m_settings.getSubsetList();
        this.parameterList = m_settings.getParameterList();
        this.setTitle("Add a new plot.");        
        JPanel content = createContentPanel();
        this.getContentPane().add(content);
        this.pack();
        this.setLocationRelativeTo(getParent());
	}


	private JPanel createContentPanel() {
		//Create the panel
		contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		contentPanel.setBackground(Color.RED);
		c.anchor = GridBagConstraints.NORTHWEST;
		//Create children
		previewPanel = new FakePlot(null);
		
		c.gridx = 0;
		c.gridy = 0;
		contentPanel.add(previewPanel, c);
		
		c.gridx = 0;
		c.gridy = 1;
		Component plotOptionsPanel = createPlotOptionsPanel();
		contentPanel.add(plotOptionsPanel, c);
		
		c.gridx = 0;
		c.gridy = 2;
		contentPanel.add(createHorizontalAxisGroup(), c);
		
		c.gridx = 0;
		c.gridy = 3;
		contentPanel.add(createVerticalAxisGroup(), c);
		
        JPanel buttonPanel = new JPanel(new FlowLayout());
        m_okButton = createOkButton();
        m_cancelButton = createCancelButton();
        buttonPanel.add(m_okButton);
        buttonPanel.add(m_cancelButton);
        
        
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.SOUTHEAST;
        c2.gridx = 0;
        c2.gridy = 4;
		contentPanel.add(buttonPanel, c2);
        
		
		Dimension preferredSize = new Dimension(500, 600);
		contentPanel.setPreferredSize(preferredSize);
		return contentPanel;
	}


	   private JPanel createPlotOptionsPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"General Options"));
		parentSelectorBox = createParentSelector();
		panel.add(parentSelectorBox);
		plotTypeSelectorBox = createPlotTypeSelector();
		panel.add(plotTypeSelectorBox);
		return panel;
	}


	private Component createVerticalAxisGroup() {
			verticalAxisGroup = new JPanel();
			verticalAxisGroup.setLayout(new FlowLayout());
			verticalAxisGroup.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					"Vertical Axis"));
			String[] verticalOptions = getParameterList();
			verticalParameterBox = new JComboBox<String>(verticalOptions);
			verticalParameterBox.setSelectedIndex(guessVerticalValueIndex(verticalOptions));
			verticalParameterBox.addActionListener(new ActionListener() {
				@Override
	            public void actionPerformed(final ActionEvent e) {
					spec.setHorizontalAxis((String)horizontalParameterBox.getModel().getSelectedItem());
	            
	            }
	        });
			verticalAxisGroup.add(verticalParameterBox);
			return verticalAxisGroup;
	}


	private int guessVerticalValueIndex(String[] verticalOptions) {
		// TODO guess better.
		return 1;
	}

	private Component createHorizontalAxisGroup() {
		horizontalAxisGroup = new JPanel();
		horizontalAxisGroup.setLayout(new FlowLayout());
		String[] horizontalOptions = getParameterList();
		horizontalAxisGroup.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Horizontal Axis"));
		horizontalParameterBox = new JComboBox<String>(horizontalOptions);
		horizontalParameterBox.setSelectedIndex(guessHorizontalValueIndex(horizontalOptions));
		horizontalParameterBox.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				spec.setHorizontalAxis((String)horizontalParameterBox.getModel().getSelectedItem());
            
            }
        });
		horizontalAxisGroup.add(horizontalParameterBox);
		return horizontalAxisGroup;
	}


	private String[] getParameterList() {
		ArrayList<String> options = new ArrayList<String>();
		for (String name:parameterList){
			options.add(name);
		}
		options.add("Rank");
		return options.toArray(new String[options.size()]);
	}


	private int guessHorizontalValueIndex(String[] horizontalOptions) {
		// TODO guess better.
		return 0;
	}


	private JComboBox<String> createPlotTypeSelector() {
		plotTypeSelectorBox = new JComboBox<String>(new String[] {DEFAULT_PLOT_TYPE});
		plotTypeSelectorBox.setSelectedIndex(0);
		plotTypeSelectorBox.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				spec.setPlotType((String)plotTypeSelectorBox.getModel().getSelectedItem());
            
            }
        });
		return plotTypeSelectorBox;
	}


	private JComboBox<String> createParentSelector() {
		parentSelectorBox = new JComboBox<String>(new String[] {DEFAULT_PARENT});
		parentSelectorBox.setSelectedIndex(0);
		parentSelectorBox.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				spec.setParent((String)parentSelectorBox.getModel().getSelectedItem());
            
            }
        });
		return parentSelectorBox;
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
            isOK = true;
            setVisible(false);
            }
        });
        return m_okButton;
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
	
	public void save() {
		m_settings.addPlotSpec(spec);
	}
}
//EOF