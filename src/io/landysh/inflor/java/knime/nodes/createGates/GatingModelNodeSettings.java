package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import io.landysh.inflor.java.core.gatingML.gates.GatingStrategy;
import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.core.plots.SubsetSpec;

public class GatingModelNodeSettings {

	private static final String ANALYSIS_KEYS = "analyses";

	private static final String PLOT_KEYS_KEY = "views";

	private static final String MODE_KEY = "AnalysisMode";

	private static final String GATING_ML_KEY = "GatingML";
	private static final String SELECTED_COLUMN_KEY = "Selected Column";

	private static final String SELECTED_COLUMN_DEFAULT = "None";
	private static final String SELECTED_SAMPLE_KEY = "Selected Sample";

	private static final String PARAMETER_LIST_KEY = "Parameter List";
	ConcurrentHashMap<String, GatingStrategy> m_analyses;
	private String mode = "FILTER";

	private String gml;
	private String selectedColumn;

	private String selectedSample;
	private String[] paramterList;

	private Hashtable<String, PlotSpec> m_plotSpecs;
	private Hashtable<String, SubsetSpec> m_subsetSpecs;

	public void addPlotSpec(PlotSpec spec) {
		m_plotSpecs.put(spec.uuid, spec);
	}

	private String[] getAnalysisKeys(ConcurrentHashMap<String, GatingStrategy> plan) {
		final ArrayList<String> ids = new ArrayList<String>();
		for (final String key : plan.keySet()) {
			ids.add(plan.get(key).getId());
		}
		return ids.toArray(new String[ids.size()]);
	}

	public String[] getParameterList() {
		return paramterList;
	}

	private String[] getPlotIDs(Hashtable<String, PlotSpec> plotSpecs2) {
		final ArrayList<String> ids = new ArrayList<String>();
		for (final String key : m_plotSpecs.keySet()) {
			ids.add(m_plotSpecs.get(key).uuid);
		}
		return ids.toArray(new String[ids.size()]);
	}

	public String getSelectedColumn() {
		return selectedColumn;
	}

	public SettingsModelString getSelectedColumnSettingsModel() {
		// TODO: Where is this used?
		return new SettingsModelString(SELECTED_COLUMN_KEY, SELECTED_COLUMN_DEFAULT);
	}

	public String[] getSubsetList() {
		final ArrayList<String> ids = new ArrayList<String>();
		ids.add("Overview");
		if (m_subsetSpecs != null) {
			for (final String key : m_subsetSpecs.keySet()) {
				ids.add(m_subsetSpecs.get(key).uuid);
			}
		}
		return ids.toArray(new String[ids.size()]);
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {

		m_plotSpecs = loadPlots(settings);
		m_analyses = loadAnalyses(settings);
		mode = settings.getString(MODE_KEY);
		gml = settings.getString(GATING_ML_KEY);
		selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
		paramterList = settings.getStringArray(PARAMETER_LIST_KEY);
	}

	private ConcurrentHashMap<String, GatingStrategy> loadAnalyses(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final ConcurrentHashMap<String, GatingStrategy> newAnalyses = new ConcurrentHashMap<String, GatingStrategy>();
		for (final String analysisKey : settings.getStringArray(ANALYSIS_KEYS)) {
			final GatingStrategy gs = GatingStrategy.load(settings.getString(analysisKey));
			newAnalyses.put(gs.getId(), gs);
		}
		return newAnalyses;
	}

	private Hashtable<String, PlotSpec> loadPlots(NodeSettingsRO settings) throws InvalidSettingsException {
		final Hashtable<String, PlotSpec> loadedPlots = new Hashtable<String, PlotSpec>();
		for (final String plotKey : settings.getStringArray(PLOT_KEYS_KEY)) {
			final PlotSpec spec = new PlotSpec(null);
			spec.loadFromString(settings.getString(plotKey));
			loadedPlots.put(spec.uuid, spec);
		}
		return loadedPlots;
	}

	public void removePlotSpec(String uuidToRemove) {
		m_plotSpecs.remove(uuidToRemove);
	}

	public void save(NodeSettingsWO settings) {

		savePlots(settings);
		saveGates(settings);

		final String[] gateListArray = getAnalysisKeys(m_analyses);
		settings.addStringArray(ANALYSIS_KEYS, gateListArray);
		final String[] plotList = getPlotIDs(m_plotSpecs);
		settings.addStringArray(PLOT_KEYS_KEY, plotList);
		settings.addString(SELECTED_SAMPLE_KEY, selectedSample);
		settings.addStringArray(PARAMETER_LIST_KEY, paramterList);
		settings.addString(MODE_KEY, mode);
		settings.addString(GATING_ML_KEY, gml);
		settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
		settings.addStringArray("Foo", getSubsetList());
	}

	private void saveGates(NodeSettingsWO settings) {
		for (final String key : m_analyses.keySet()) {
			settings.addString(key, m_analyses.get(key).toGatingML().toString());
		}
	}

	private void savePlots(NodeSettingsWO settings) {
		for (final String key : m_plotSpecs.keySet()) {
			settings.addString(key, m_plotSpecs.get(key).saveToString());
		}
	}

	public void setParameterList(String[] newValues) {
		paramterList = newValues;

	}

	public void setSelectedColumn(String newColumn) {
		selectedColumn = newColumn;

	}

	public void setSelectedSample(String newValue) {
		selectedSample = newValue;
	}

	public void validate(NodeSettingsRO settings) {

	}
}
// EOF