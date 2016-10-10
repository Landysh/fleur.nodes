package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import io.landysh.inflor.java.core.subsets.AbstractSubset;
import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;
import sun.awt.windows.WEmbeddedFrame;

public class GateEditorDialog extends JDialog{

	/**
	 * The modal dialog from which new chart definitions will be created 
	 * and existing charts may be edited
	 */

	private static final long serialVersionUID = 3249082301592821578L;
	// private static final Frame parent;
	protected ChartPanel panel;

	AbstractSubset subset;

	private JButton m_okButton = null;
	private JButton m_cancelButton = null;
	public boolean isOK = false;
	private String gateName = "Default";


	public GateEditorDialog(Frame topFrame) {
		
		/**
		 * Use this constructor to create a new chart. 
		 * 
		 * @param topFrame the frame in which this dialog resides.  Required to make dialog modal
		 * @param parent the parent dialog which stores the data model.
		 * @param id The UUID of the domain object. typically found in the settingsModel.
		 */
		// Initialize
		super(topFrame);
		setModal(true);

		// populate the dialog
		setTitle("Create gate");
		final JPanel content = createContentPanel();
		getContentPane().add(content);
		pack();
		setLocationRelativeTo(getParent());
	}

	public GateEditorDialog(WEmbeddedFrame topFrame, CreateGatesNodeDialog parent, String id) {
		/**
		 * Use this constructor to edit an existing chart. 
		 * 
		 * @param topFrame the frame in which this dialog resides.  Required to make dialog modal
		 * @param parent the parent dialog which stores the data model.
		 * @param id The UUID of the domain object. typically found in the settingsModel.
		 */
		super(topFrame);
		// populate the dialog
		setTitle("Editing: ...");
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
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		//Button Panel
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridy = 4;
		final JPanel buttonPanel = new JPanel(new FlowLayout());
		m_okButton = createOkButton();
		m_cancelButton = createCancelButton();
		buttonPanel.add(m_okButton);
		buttonPanel.add(m_cancelButton);
				
		contentPanel.add(buttonPanel, gbc);
		contentPanel.setPreferredSize(new Dimension(300, 450));
		
		return contentPanel;
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

	public String getGateName() {
		return gateName;
	}
}
