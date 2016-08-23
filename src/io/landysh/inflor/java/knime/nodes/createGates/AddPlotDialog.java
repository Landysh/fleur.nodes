package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class AddPlotDialog extends JDialog{
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 3249082301592821578L;
	private static final String DEFAULT_PARENT = "UNGATED";
	private static final String DEFAULT_PLOT_TYPE = "FAKE";
	private static Frame parent;
	JPanel previewPanel;
	JPanel settingsPanel;
	JPanel contentPanel;
	
	PlotSpec spec;
	
	String[] populationList;
	String[] parameterList;
	
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
        super(parent);
        this.m_settings = settings;
        setModal(true);
        this.parameterList = m_settings.getParameterList();
        this.setTitle("Add a new plot.");
        this.setContentPane(createContentPane());
        this.m_okButton = createOkButton();
        this.m_cancelButton = createCancelButton();
        this.add(m_okButton);
        this.add(m_cancelButton);
        this.pack();
        this.setLocationRelativeTo(getParent());
	}


	private Container createContentPane() {
		previewPanel = new FakePlot(null);
		settingsPanel = createSettingsPanel();
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(previewPanel);
		contentPanel.add(settingsPanel);
		
		return contentPanel;
	}


	private JPanel createSettingsPanel() {
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(createParentSelector());
		settingsPanel.add(createPlotTypeSelector());
		settingsPanel.add(createHorizontalAxisGroup());
		settingsPanel.add(createVerticalAxisGroup());
		return settingsPanel;
	}

	   private Component createVerticalAxisGroup() {
			verticalAxisGroup = new JPanel();
			verticalAxisGroup.setLayout(new BoxLayout(verticalAxisGroup, BoxLayout.X_AXIS));
			String[] verticalOptions = getParameterList();
			verticalParameterBox = new JComboBox<String>(verticalOptions);
			verticalParameterBox.setSelectedIndex(guessVerticalValueIndex(verticalOptions));
			verticalParameterBox.addActionListener(new ActionListener() {
				@Override
	            public void actionPerformed(final ActionEvent e) {
					spec.setHorizontalAxis((String)horizontalParameterBox.getModel().getSelectedItem());
	            
	            }
	        });
			return horizontalParameterBox;
	}


	private int guessVerticalValueIndex(String[] verticalOptions) {
		// TODO guess better.
		return 1;
	}

	private Component createHorizontalAxisGroup() {
		horizontalAxisGroup = new JPanel();
		horizontalAxisGroup.setLayout(new BoxLayout(horizontalAxisGroup, BoxLayout.X_AXIS));
		String[] horizontalOptions = getParameterList();
		horizontalParameterBox = new JComboBox<String>(horizontalOptions);
		horizontalParameterBox.setSelectedIndex(guessHorizontalValueIndex(horizontalOptions));
		horizontalParameterBox.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				spec.setHorizontalAxis((String)horizontalParameterBox.getModel().getSelectedItem());
            
            }
        });
		return horizontalParameterBox;
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


	private Component createPlotTypeSelector() {
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


	private Component createParentSelector() {
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