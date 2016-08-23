package io.landysh.inflor.java.knime.nodes.createGates;

import java.awt.Color;
import java.awt.Graphics;

public class FakePlot extends AbstractEventPlot{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3697138610426712126L;

	public FakePlot(String priorUUID){
		super(priorUUID);
	}
	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.GRAY);
        g.fillRect(20, 20, 25, 25);
        g.drawRect(10, 10, 256, 256);
    }
}
