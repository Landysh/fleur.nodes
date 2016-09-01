package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;

public class AddPlotActionListener implements ActionListener {

	private final CreateGatesNodeDialog dialog;

	public AddPlotActionListener(CreateGatesNodeDialog currentDialog) {
		dialog = currentDialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.addPlot();

	}

}
