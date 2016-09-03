package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import org.jfree.chart.plot.XYPlot;

public abstract class AbstractFCSPlot extends XYPlot{

	/**
	 * @Param newUUID creates a new UUID for this plot definition.
	 */

	private static final long serialVersionUID = 2722144657680392136L;

	public final String uuid;

	public AbstractFCSPlot(String priorUUID) {
		// Create new UUID if needed.
		if (priorUUID == null) {
			uuid = UUID.randomUUID().toString();
		} else {
			uuid = priorUUID;
		}
	}
	
	public abstract void update(PlotSpec spec);

}
