package io.landysh.inflor.java.knime.nodes.transform;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.knime.core.NodeUtilities;

public class TransformNodeSettings {

	private static final String TRANSFORM_MAP_KEY = "Transfomations";

	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	
	private String m_selectedColumn;
	private TreeMap<String, AbstractTransform> m_transforms = new TreeMap<String, AbstractTransform>();

	public TransformNodeSettings (){
	}

	public void save(NodeSettingsWO settings) {
		settings.addString(SELECTED_COLUMN_KEY, m_selectedColumn);
		NodeUtilities.saveSerializable(settings, TRANSFORM_MAP_KEY, m_transforms);
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		m_selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		Map<String, Serializable> serMap = NodeUtilities.loadHashMap(settings, TRANSFORM_MAP_KEY);
		TreeMap<String, AbstractTransform> loadedTransforms  = new TreeMap<String, AbstractTransform>();
		for (Entry<String,Serializable> e:serMap.entrySet()){
			loadedTransforms.put(e.getKey(),(AbstractTransform) e.getValue());
		}
		m_transforms = loadedTransforms;
	}

	public void addTransform(String key, AbstractTransform value){m_transforms.put(key, value);}
	public void removeTransform(String key)						 {m_transforms.remove(key);}
	public void setSelectedColumn(String selectedItem) 			 {m_selectedColumn = selectedItem;}
	public String getSelectedColumn() 							 {return m_selectedColumn;}

	public AbstractTransform getTransform(String name) {return m_transforms.get(name);}
	public void setTransform(AbstractTransform newValue, String key) {this.m_transforms.put(key, newValue);}

	public TreeMap<String, AbstractTransform> getAllTransorms() {return this.m_transforms;}

	public void validate(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
	}

}
