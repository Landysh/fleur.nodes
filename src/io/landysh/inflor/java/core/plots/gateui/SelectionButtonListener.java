package io.landysh.inflor.java.core.plots.gateui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import io.landysh.inflor.java.core.plots.FCSChartPanel;

public class SelectionButtonListener implements ActionListener {

	FCSChartPanel panel;
	GateCreationToolBar toolbar;
	private GateSelectionAdapter mouseAdapter;
	public SelectionButtonListener(FCSChartPanel panel, GateCreationToolBar toolbar, GateSelectionAdapter mouseAdapter){
		this.panel = panel;
		this.toolbar = toolbar;
		this.mouseAdapter = mouseAdapter;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		toolbar.removeAllListeners();

		toolbar.cursorButtons.forEach(button -> button.setEnabled(true));
		((JButton)e.getSource()).setEnabled(false);
		
		panel.addMouseListener(mouseAdapter);
		panel.addMouseMotionListener(mouseAdapter);
	}
}
