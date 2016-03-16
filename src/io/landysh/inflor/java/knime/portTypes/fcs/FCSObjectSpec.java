package io.landysh.inflor.java.knime.portTypes.fcs;

import java.util.Hashtable;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;

public class FCSObjectSpec implements PortObjectSpec {
	public Hashtable<String, String> 	header;
	public String[] 					parameterList;
	
	public FCSObjectSpec(Hashtable<String, String> keywords, String[] plist) {
		header = keywords;
		parameterList = plist;
	}

	@Override
	public JComponent[] getViews() {
        return new JComponent[]{new FCSSummaryView(header)};
	}

}
