package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.subsets.AbstractSubset;

public class GatingModelNodeSettings {

	private static final String CHARTS_SETTINGS_KEY  = "Charts";
	private static final String GATES_SETTINGS_KEY = "Filters";
	private static final String SUBSETS_SETTINGS_KEY = "Subsets";

	private static final String SELECTED_MODE_KEY = "AnalysisMode";
	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private static final String SELECTED_SAMPLE_KEY = "Selected Sample";
	
	private String mode = "FILTER";
	private String selectedColumn;
	private String selectedSample;
	
	private Hashtable<String, ChartSpec> 		m_charts;
	private Hashtable<String, AbstractSubset>	m_subsets;
	private Hashtable<String, AbstractGate> 	m_gates;

	public GatingModelNodeSettings (){
		m_charts  = new Hashtable<String, ChartSpec> ();		
		m_subsets = new Hashtable<String, AbstractSubset>();
		m_gates   = new Hashtable<String, AbstractGate>();
	}

	public void save(NodeSettingsWO settings) throws IOException {
		
		settings.addString(SELECTED_SAMPLE_KEY, selectedSample);
		settings.addString(SELECTED_MODE_KEY, mode);
		settings.addString(SELECTED_COLUMN_KEY, selectedColumn);

		saveHashtable(settings, CHARTS_SETTINGS_KEY, m_charts);
		saveHashtable(settings, GATES_SETTINGS_KEY, m_gates);
		saveHashtable(settings, SUBSETS_SETTINGS_KEY, m_subsets);
		
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		m_gates = loadFilters(settings);
		m_charts = loadPlots(settings);
		m_subsets = loadSubsets(settings);
		mode = settings.getString(SELECTED_MODE_KEY);
		selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		selectedSample = settings.getString(SELECTED_SAMPLE_KEY);
	}

	@SuppressWarnings("unchecked")//TODO as with other hashtable serializers
	private Hashtable<String, AbstractSubset> loadSubsets(NodeSettingsRO settings) throws InvalidSettingsException {
		byte[] bytes = settings.getByteArray(SUBSETS_SETTINGS_KEY);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		Object loadedObject = null;
		Hashtable<String, AbstractSubset> loadedSubsets = null;
		try {
			ois = new ObjectInputStream(bis);
			loadedObject = ois.readObject();
			if (loadedObject instanceof Hashtable) {
				loadedSubsets = (Hashtable<String, AbstractSubset>) loadedObject;
			} else {
				throw new IOException();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Unable to parse chart specs from settings");
		}
		return loadedSubsets;
	}

	@SuppressWarnings("unchecked")//TODO as with other hashtable serializers
	private Hashtable<String, AbstractGate> loadFilters(NodeSettingsRO settings)
			throws InvalidSettingsException {
		byte[] bytes = settings.getByteArray(SUBSETS_SETTINGS_KEY);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		Object loadedObject = null;
		Hashtable<String, AbstractGate> loadedFilters = null;
		try {
			ois = new ObjectInputStream(bis);
			loadedObject = ois.readObject();
			if (loadedObject instanceof Hashtable) {
				loadedFilters = (Hashtable<String, AbstractGate>) loadedObject;
			} else {
				throw new IOException();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Unable to parse event filters from settings");
		}
		return loadedFilters;
	}

	@SuppressWarnings("unchecked")//TODO: Not sure how to check the key/value types in a hashtable.
	private Hashtable<String, ChartSpec> loadPlots(NodeSettingsRO settings) throws InvalidSettingsException{
		
		byte[] chartBytes = settings.getByteArray(CHARTS_SETTINGS_KEY);
		ByteArrayInputStream bis = new ByteArrayInputStream(chartBytes);
		ObjectInputStream ois;
		Object loadedObject = null;
		Hashtable<String, ChartSpec> loadedChartSpecs = null;
		try {
			ois = new ObjectInputStream(bis);
			loadedObject = ois.readObject();
			if (loadedObject instanceof Hashtable) {
				loadedChartSpecs = (Hashtable<String, ChartSpec>) loadedObject;
			} else {
				throw new IOException();
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Unable to parse chart specs from settings");
		}
		return loadedChartSpecs;
	}

	public void removePlotSpec(String uuidToRemove) {
		m_charts.remove(uuidToRemove);
	}

	private void saveHashtable(NodeSettingsWO settings, String key, Serializable obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		byte[] chartBytes = bos.toByteArray();
		settings.addByteArray(key, chartBytes);
	}

	public void setSelectedColumn(String newColumn) {
		selectedColumn = newColumn;

	}

	public void setSelectedSample(String newValue) {
		selectedSample = newValue;
	}

	public Hashtable<String, ChartSpec> getPlotSpecs() {
		return m_charts;
	}

	public void deleteChart(String id) {
		m_charts.remove(id);
	}

	public void addPlotSpec(ChartSpec spec) {
		m_charts.put(spec.ID, spec);
	}

	public String getSelectedColumn() {
		return selectedColumn;
	}
	
	
	public ChartSpec getChartSpec(String id) {
		return m_charts.get(id);
	}

	public void validate(NodeSettingsRO settings) {
		// TODO Auto-generated method stub
	}
}
// EOF