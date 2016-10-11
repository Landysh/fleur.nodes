package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

@SuppressWarnings("serial")
public class GateNameEditor extends JDialog{

	private static final boolean NAME_EDITABLE = true;
	private static final List<String> DEFAULT_GATE_NAMES = Arrays.asList(
			new String[]{"Scatter",
						 "Live",
						 "Singlets",
						 "Lymphs",
						 "T Cells",
						 "B Cells"});

	protected ChartPanel panel;

	private JButton m_okButton = null;
	private JButton m_cancelButton = null;
	public boolean isOK = false;
	private String gateName = "";
	

	public GateNameEditor(Frame topFrame) {
		
		/**
		 * @param topFrame the frame in which this dialog resides.  Required to make dialog modal
		 */
		super(topFrame);
		setModal(true);
		setTitle("Create gate");
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
		JPanel contentPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		
		//Gate name selector
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx=1;
		gbc.gridy=1;
		JComboBox<String> gateNameBox = createGateNameBox();
		contentPanel.add(gateNameBox,gbc);

		//Button Panel
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridy = 2;
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		m_okButton = createOkButton();
		m_cancelButton = createCancelButton();
		buttonPanel.add(m_okButton);
		buttonPanel.add(m_cancelButton);
		contentPanel.add(buttonPanel, gbc);
		
		return contentPanel;
	}

	private JComboBox<String> createGateNameBox() {
		JComboBox<String> comboBox = new JComboBox<String>();
		DEFAULT_GATE_NAMES.stream()
		                  .sequential()
		                  .forEachOrdered(name -> comboBox.addItem(name));
		
		comboBox.setEditable(NAME_EDITABLE);
		
		comboBox.addActionListener(new ActionListener(){	
			@Override
			public void actionPerformed(ActionEvent e) {
				gateName = (String) comboBox.getSelectedItem();
			}
		});
		comboBox.setSelectedIndex(0);
		return comboBox;
	}

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

	public String getGateName() {
		return gateName;
	}
}
