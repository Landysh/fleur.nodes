package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.java.core.gatingML.gates.GatingStrategy;
import io.landysh.inflor.java.core.plots.AbstractFACSPlot;

public class GatingModelNodeSettings {
	
	ConcurrentHashMap<String, GatingStrategy> 	m_analyses;
	
	private static final String	 ANALYSIS_KEYS = "analyses";
	
	private static final String PLOT_KEYS_KEY = "views";

	private static final String MODE_KEY = "AnalysisMode";
	private String  mode = "FILTER";	
	
	private static final String GATING_ML_KEY = "GatingML";
	private String   gml;
	
	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private String   selectedColumn;
	private static final String SELECTED_COLUMN_DEFAULT = "None";

	private static final String SELECTED_SAMPLE_KEY = "Selected Sample";
	private String selectedSample;
	
	private static final String PARAMETER_LIST_KEY = "Parameter List";
	private String[] paramterList;
	
	private Hashtable<String, PlotSpec> plotSpecs;
	
	public void save(NodeSettingsWO settings) {
		
		savePlots(settings);
		saveGates(settings);
		
		String[] gateListArray = getAnalysisKeys(m_analyses);
		settings.addStringArray(ANALYSIS_KEYS, gateListArray);
		String[] plotList = getPlotIDs(plotSpecs);
		settings.addStringArray(PLOT_KEYS_KEY, plotList);
		settings.addString(SELECTED_SAMPLE_KEY, selectedSample);
		settings.addStringArray(PARAMETER_LIST_KEY, paramterList);
		settings.addString(MODE_KEY, mode);
		settings.addString(GATING_ML_KEY, gml);
		settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
	}

	private String[] getPlotIDs(Hashtable<String, PlotSpec> plotSpecs2) {
		ArrayList<String> ids = new ArrayList<String>();
		for (String key:plotSpecs.keySet()){
			ids.add(plotSpecs.get(key).uuid);
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
		for(String key:plotSpecs.keySet()){
			settings.addString(key, plotSpecs.get(key).saveToString());
		}		
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		
		plotSpecs = loadPlots(settings);
		m_analyses = loadAnalyses(settings);
		mode = settings.getString(MODE_KEY);
		gml = settings.getString(GATING_ML_KEY);
		selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		paramterList = settings.getStringArray(PARAMETER_LIST_KEY);
	}

	private ConcurrentHashMap<String, GatingStrategy> loadAnalyses(NodeSettingsRO settings) throws InvalidSettingsException {
		ConcurrentHashMap <String, GatingStrategy> newAnalyses = new ConcurrentHashMap<String, GatingStrategy>();
		for (String analysisKey:settings.getStringArray(ANALYSIS_KEYS)){
			GatingStrategy gs = GatingStrategy.load(settings.getString(analysisKey));
			newAnalyses.put(gs.getId(), gs);
		}
		return newAnalyses;
	}

	private Hashtable<String, PlotSpec> loadPlots(NodeSettingsRO settings) throws InvalidSettingsException {
		Hashtable <String, PlotSpec> loadedPlots = new Hashtable<String, PlotSpec>();
		for (String plotKey:settings.getStringArray(PLOT_KEYS_KEY)){
			PlotSpec spec = PlotSpec.load(settings.getString(plotKey));
			loadedPlots.put(spec.uuid, spec);
		}
		return loadedPlots;
	}

	public void validate(NodeSettingsRO settings){
		
	}

	public String getSelectedColumn() {
		return selectedColumn;
	}

	public SettingsModelString getSelectedColumnSettingsModel() {
		//TODO: Where is this used?
		return new SettingsModelString(SELECTED_COLUMN_KEY, SELECTED_COLUMN_DEFAULT);
	}

	public void setSelectedColumn(String newColumn) {
		this.selectedColumn = newColumn;
		
	}

	public void setSelectedSample(String newValue) {
		this.selectedSample = newValue;
	}

	public String[] getParameterList() {
		return this.paramterList;
	}

	public void setParameterList(String[] newValues) {
		this.paramterList = newValues;
		
	}

	public void addPlotSpec(PlotSpec spec) {
		this.plotSpecs.put(spec.uuid, spec);
	}
	
	public void removePlotSpec(String uuidToRemove) {
		this.plotSpecs.remove(uuidToRemove);
	}
}
//EOF