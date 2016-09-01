package io.landysh.inflor.java.core.plots;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FakePlot extends AbstractFCSPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3697138610426712126L;
	private JPanel panel;

	public FakePlot(String priorUUID) {
		super(priorUUID);
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.setBackground(Color.BLUE);
	}

	@Override
	public void update(PlotSpec spec) {
		this.panel.setBackground(Color.GREEN);
	}

	@Override
	public JPanel getPanel() {
		return this.panel;
	}
}
