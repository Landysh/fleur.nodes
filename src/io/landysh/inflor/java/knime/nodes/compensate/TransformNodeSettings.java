package io.landysh.inflor.java.knime.nodes.compensate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.transforms.AbstractTransform;

public class TransformNodeSettings {

	private static final String TRANSFORM_MAP_KEY = "Transfomations";

	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private static final String SELECTED_COMP_Reference = "complensation reference";
	
	private String m_selectedColumn;
	private String m_selectedSample;
	private HashMap<String, AbstractTransform> 	m_transforms = new HashMap<>();

	public TransformNodeSettings (){
	}

	public void save(NodeSettingsWO settings) throws IOException {
		
		settings.addString(SELECTED_COMP_Reference, m_selectedSample);
		settings.addString(SELECTED_COLUMN_KEY, m_selectedColumn);
		saveSerializable(settings, TRANSFORM_MAP_KEY, m_transforms);
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		m_selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		m_selectedSample = settings.getString(SELECTED_COMP_Reference);
		String[] transformKeyArray = settings.getStringArray(TRANSFORM_MAP_KEY);
		m_transforms = loadHashMap(settings, transformKeyArray);
	}


	
	public void addTransform(String key, AbstractTransform value){
		m_transforms.put(key, value);
	}
	public void removeTransform(String key){
		m_transforms.remove(key);
	}
}
