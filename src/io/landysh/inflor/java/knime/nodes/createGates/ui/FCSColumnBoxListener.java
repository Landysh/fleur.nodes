package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;

public class FCSColumnBoxListener implements ActionListener {
	CreateGatesNodeDialog dialog;

	public FCSColumnBoxListener(CreateGatesNodeDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.m_Settings.setSelectedColumn((String) dialog.fcsColumnBox.getModel().getSelectedItem());
	}
}
