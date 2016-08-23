package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.Hashtable;

import javax.swing.JPanel;

public class LineageAnalysisPanel extends JPanel {

	/**
	 *  Panel which stores and controls the layout of the plots in a given lineage analysis.
	 */
	
	Hashtable<String, AbstractEventPlot> plotPanels;
	
	private static final long serialVersionUID = 7947589954322305645L;
	
	public LineageAnalysisPanel(){
		
	}
	
	public void addPlot(AbstractEventPlot plot){
		plotPanels.put(plot.uuid, plot);
		updateLayout(plotPanels);
	}
	
	public void removePlot(String uuid){
		this.plotPanels.remove(uuid);
	}
	
	public void updateLayout(Hashtable<String, AbstractEventPlot> panels){
		super.removeAll();
		for (String uuid:panels.keySet()){
			this.add(panels.get(uuid));
		}
	}
}
