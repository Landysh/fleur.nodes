package io.landysh.inflor.java.knime.nodes.compensate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.knime.core.NodeUtilities;

public class CompensateNodeSettings {

	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private static final String SPILLOVER_KEY = "Complensation reference";
	
	private String m_selectedColumn;
	private HashMap<String, String> m_spilloverRef;

	public CompensateNodeSettings (){
	}

	public void save(NodeSettingsWO settings) {
		NodeUtilities.saveSerializable(settings, SPILLOVER_KEY, m_spilloverRef);
		settings.addString(SELECTED_COLUMN_KEY, m_selectedColumn);
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		m_selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		HashMap<String, Serializable> map = NodeUtilities.loadHashMap(settings, SPILLOVER_KEY);
		m_spilloverRef = new HashMap<String, String>();
		for (Entry<String, Serializable> e:map.entrySet()){
			m_spilloverRef.put(e.getKey(), (String) e.getValue());
		}
	}
	
	public String getSelectedColumn() 				   {return m_selectedColumn;}
	public void setSelectedColumn(String selectedItem) {m_selectedColumn = selectedItem;}

	public void setHeader(HashMap<String, String> keywords) {
		m_spilloverRef = keywords;
	}

	public void validate(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getReferenceHeader() {
		return m_spilloverRef;
	}
}
