package io.landysh.inflor.java.core.plots;

import javax.swing.JPanel;

import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

public class ContourPlot extends AbstractFCSPlot {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6629488875666461173L;

	public ContourPlot(String priorUUID) {
		super(priorUUID);
	}

	public ContourPlot(PlotSpec plotSpec, String priorUUID) {
		this(priorUUID);
		XYItemRenderer renderer = new XYBlockRenderer();
		this.setRenderer(renderer);
		
	}

	@Override
	public void update(PlotSpec spec) {

	}

}
