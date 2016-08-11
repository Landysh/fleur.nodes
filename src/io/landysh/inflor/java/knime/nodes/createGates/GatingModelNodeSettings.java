package io.landysh.inflor.java.knime.nodes.createGates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.plots.AbstractFACSPlot;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class GatingModelNodeSettings {
	
	ConcurrentHashMap<String, AbstractGate> gates;
	ConcurrentHashMap<String, AbstractFACSPlot> plots;

	
	private static final String	 GATE_LIST_KEY = "Gate List";
	private ArrayList<String> gateList;
	
	private static final String PLOT_LIST_KEY = "Plot List";
	private ArrayList<String> plotList;

	private static final String MODE_KEY = "Gate Mode";
	private String  mode = "FILTER";	
	
	private static final String GATING_ML_KEY = "GatingML";
	private String   gml;
	
	private static final String SELECTED_COLUMN_KEY = "Selected Column";
	private String   selectedColumn;
	
	public void save(NodeSettingsWO settings) {
		
		savePlots(settings);
		saveGates(settings);
		
		String[] gateListArray = getGateIDs(gates);
		settings.addStringArray(GATE_LIST_KEY, gateListArray);
		
		String[] plotListArray = getPlotIDs(plots);
		settings.addStringArray(PLOT_LIST_KEY, plotListArray);
		
		settings.addString(MODE_KEY, mode);
		settings.addString(GATING_ML_KEY, gml);
		settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
	}

	private String[] getPlotIDs(ConcurrentHashMap<String, AbstractFACSPlot> plots2) {
		// TODO Auto-generated method stub
		return null;
	}

	private String[] getGateIDs(ConcurrentHashMap<String, AbstractGate> gates2) {
		// TODO Auto-generated method stub
		return null;
	}

	private void saveGates(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		
	}

	private void savePlots(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		gateList = new ArrayList<String>(Arrays.asList(settings.getStringArray(GATE_LIST_KEY)));
		plotList = new ArrayList<String>(Arrays.asList(settings.getStringArray(PLOT_LIST_KEY)));
		
		plots = loadPlots(settings, plotList);
		gates = loadGates(settings, gateList);
		
		mode = settings.getString(MODE_KEY);
		gml = settings.getString(GATING_ML_KEY);
		selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
	}

	private ConcurrentHashMap<String, AbstractGate> loadGates(NodeSettingsRO settings, ArrayList<String> gateList2) {
		// TODO Auto-generated method stub
		return null;
	}

	private ConcurrentHashMap<String, AbstractFACSPlot> loadPlots(NodeSettingsRO settings,
			ArrayList<String> plotList2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void validate(NodeSettingsRO settings){
		
	}
}
//EOF