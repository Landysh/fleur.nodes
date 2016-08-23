package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.UUID;

import javax.swing.JPanel;

public abstract class AbstractEventPlot extends JPanel{
	
	/**
	 * @Param newUUID creates a new UUID for this plot definition.
	 */
	
	private static final long serialVersionUID = 2722144657680392136L;
	
	final String uuid;
	
	public AbstractEventPlot(String priorUUID){
	    //Create new UUID if needed. 
		if (priorUUID==null){
	    	uuid = UUID.randomUUID().toString();
	    } else {
	    	uuid = priorUUID;
	    }
	}
}
