package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;

import java.util.Hashtable;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;

import io.landysh.inflor.java.core.FCSSummaryView;

public class AnnotatedVectorStoreSpec implements PortObjectSpec {
	public Hashtable<String, String> 	header;
	public String[] 					parameterList;
	
	public AnnotatedVectorStoreSpec(Hashtable<String, String> keywords, String[] plist) {
		header = keywords;
		parameterList = plist;
	}

	@Override
	public JComponent[] getViews() {
        return new JComponent[]{new FCSSummaryView(header)};
	}

	public Hashtable<String, String> getHeader() {
		return header;
	}

	public String[] getParameterList() {
		return parameterList;
	}

}
