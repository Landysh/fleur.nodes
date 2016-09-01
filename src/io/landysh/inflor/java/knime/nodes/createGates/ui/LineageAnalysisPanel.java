package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.Hashtable;

import javax.swing.JPanel;

import io.landysh.inflor.java.core.plots.AbstractFCSPlot;

public class LineageAnalysisPanel extends JPanel {

	private static final long serialVersionUID = 7947589954322305645L;

	/**
	 * Panel which stores and controls the layout of the plots in a given
	 * lineage analysis.
	 */

	Hashtable<String, AbstractFCSPlot> plotPanels;

	public LineageAnalysisPanel() {

	}

	public void addPlot(AbstractFCSPlot plot) {
		plotPanels.put(plot.uuid, plot);
		updateLayout(plotPanels);
	}

	public void removePlot(String uuid) {
		plotPanels.remove(uuid);
	}

	public void updateLayout(Hashtable<String, AbstractFCSPlot> plots) {
		super.removeAll();
		for (final String uuid : plots.keySet()) {
			//TODO Add plot to panel
		}
	}
}
