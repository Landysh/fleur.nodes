package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.gatingML.gates.GatingStrategy;
import io.landysh.inflor.java.core.plots.AbstractFACSPlot;

public class GatingModelNodeSettings {
	
	ConcurrentHashMap<String, GatingStrategy> 	m_analyses;
	ConcurrentHashMap<String, AbstractFACSPlot> m_views;

	
	private static final String	 ANALYSIS_KEYS = "analyses";
	
	private static final String VIEW_KEYS = "views";

	private static final String MODE_KEY = "AnalysisMode";
	private String  mode = "FILTER";	
	
	private static final String GATING_ML_KEY = "GatingML";
	private String   gml;
	
	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private String   selectedColumn;
	
	private static final String SELECTED_SAMPLE_KEY = "Selected Sample";
	private String selectedSample;
	
	public void save(NodeSettingsWO settings) {
		
		savePlots(settings);
		saveGates(settings);
		
		String[] gateListArray = getAnalysisKeys(m_analyses);
		settings.addStringArray(ANALYSIS_KEYS, gateListArray);
		String[] viewList = getViewIDs(m_views);
		settings.addStringArray(VIEW_KEYS, viewList);
		
		settings.addString(MODE_KEY, mode);
		settings.addString(GATING_ML_KEY, gml);
		settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
	}

	private String[] getViewIDs(ConcurrentHashMap<String, AbstractFACSPlot> plots2) {
		ArrayList<String> ids = new ArrayList<String>();
		for (String key:m_views.keySet()){
			ids.add(m_views.get(key).getId());
		}		
		return ids.toArray(new String[ids.size()]);
	}

	private String[] getAnalysisKeys(ConcurrentHashMap<String, GatingStrategy> plan) {
		ArrayList<String> ids = new ArrayList<String>();
		for (String key:plan.keySet()){
			ids.add(plan.get(key).getId());
		}		
		return ids.toArray(new String[ids.size()]);
	}

	private void saveGates(NodeSettingsWO settings) {
		for(String key:m_analyses.keySet()){
			settings.addString(key, m_analyses.get(key).toGatingML().toString());
		}
	}

	private void savePlots(NodeSettingsWO settings) {
		for(String key:m_views.keySet()){
			settings.addString(key, m_views.get(key).toXML().toString());
		}		
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		
		m_views = loadViews(settings);
		m_analyses = loadAnalyses(settings);
		
		mode = settings.getString(MODE_KEY);
		gml = settings.getString(GATING_ML_KEY);
		selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
	}

	private ConcurrentHashMap<String, GatingStrategy> loadAnalyses(NodeSettingsRO settings) throws InvalidSettingsException {
		ConcurrentHashMap <String, GatingStrategy> newAnalyses = new ConcurrentHashMap<String, GatingStrategy>();
		for (String analysisKey:settings.getStringArray(ANALYSIS_KEYS)){
			GatingStrategy gs = GatingStrategy.load(settings.getString(analysisKey));
			newAnalyses.put(gs.getId(), gs);
		}
		return newAnalyses;
	}

	private ConcurrentHashMap<String, AbstractFACSPlot> loadViews(NodeSettingsRO settings) throws InvalidSettingsException {
		ConcurrentHashMap <String, AbstractFACSPlot> loadedPlots = new ConcurrentHashMap<String, AbstractFACSPlot>();
		for (String plotKey:settings.getStringArray(VIEW_KEYS)){
			AbstractFACSPlot plot = AbstractFACSPlot.load(settings.getString(plotKey));
			loadedPlots.put(plot.getId(), plot);
		}
		return loadedPlots;
	}

	public void validate(NodeSettingsRO settings){
		
	}

	public String getSelectedColumn() {
		return selectedColumn;
	}

	public StringCell getSelectedColumnSettingsModel() {
		//TODO:
		return null;
	}

	public void setSelectedColumn(String newColumn) {
		this.selectedColumn = newColumn;
		
	}

	public void setSelectedSample(String newValue) {
		this.selectedSample = newValue;
	}
}
//EOF