package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SampleBoxListener implements ActionListener {
	
	private CreateGatesNodeDialog dialog;

	public SampleBoxListener (CreateGatesNodeDialog dialog){
		this.dialog = dialog;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String newValue = (String) this.dialog.selectSampleBox.getModel().getSelectedItem();
		this.dialog.m_Settings.setSelectedSample(newValue);
	}

}
