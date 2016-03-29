package io.landysh.inflor.java.knime.portTypes.fcs;

import java.util.Hashtable;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;

public class FCSFrameSpec implements PortObjectSpec {
	public Hashtable<String, String> 	header;
	public String[] 					parameterList;
	
	public FCSFrameSpec(Hashtable<String, String> keywords, String[] plist) {
		header = keywords;
		parameterList = plist;
	}

	@Override
	public JComponent[] getViews() {
        return null;///new JComponent[]{new FCSSummaryView(header)};
	}

}
