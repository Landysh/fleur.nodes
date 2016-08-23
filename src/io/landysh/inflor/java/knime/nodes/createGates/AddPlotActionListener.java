package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPlotActionListener implements ActionListener {

	private CreateGatesNodeDialog dialog;

	public AddPlotActionListener(CreateGatesNodeDialog currentDialog) {
		this.dialog = currentDialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.dialog.addPlot();

	}

}
