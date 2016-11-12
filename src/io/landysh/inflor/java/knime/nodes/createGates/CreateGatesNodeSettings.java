package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.knime.core.NodeUtilities;

public class CreateGatesNodeSettings {

  private static final String CHARTS_SETTINGS_KEY = "Charts";
  private static final String GATES_SETTINGS_KEY = "Filters";

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private static final String SELECTED_SAMPLE_KEY = "Selected Sample";
  private static final String OVERVEIW_PLAN_KEY = "Overview Plan";

  private String selectedColumn;
  private String selectedSample;

  private HashMap<String, ChartSpec> m_charts;
  private HashMap<String, AbstractGate> modelGates;
  private HashMap<String, List<String>> modelPlans;

  public CreateGatesNodeSettings() {
    m_charts = new HashMap<String, ChartSpec>();
    modelGates = new HashMap<String, AbstractGate>();
    modelPlans  =new HashMap<String, List<String>>();
  }

  public void save(NodeSettingsWO settings) throws IOException {

    settings.addString(SELECTED_SAMPLE_KEY, selectedSample);
    settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
    NodeUtilities.saveSerializable(settings, CHARTS_SETTINGS_KEY, m_charts);
    NodeUtilities.saveSerializable(settings, GATES_SETTINGS_KEY,  modelGates);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
   modelGates = new HashMap<String, AbstractGate>();
   Map <String, Serializable> loadedMap = NodeUtilities.loadMap(settings, GATES_SETTINGS_KEY);
   for (Entry<String, Serializable> entry:loadedMap.entrySet()){
     modelGates.put(entry.getKey(), (AbstractGate) entry.getValue());
   }
   m_charts = new HashMap<>();
   loadedMap = NodeUtilities.loadMap(settings, CHARTS_SETTINGS_KEY);
   for (Entry<String, Serializable> entry:loadedMap.entrySet()){
     m_charts.put(entry.getKey(), (ChartSpec) entry.getValue());
   }
   selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
   selectedSample = settings.getString(SELECTED_SAMPLE_KEY);
  }

  public void removePlotSpec(String uuidToRemove) {
    m_charts.remove(uuidToRemove);
  }

  public void setSelectedColumn(String newColumn) {
    selectedColumn = newColumn;
  }

  public void setSelectedSample(String newValue) {
    selectedSample = newValue;
  }

  public HashMap<String, ChartSpec> getPlotSpecs() {
    return m_charts;
  }

  public void deleteChart(String id) {
    m_charts.remove(id);
  }

  public void addPlotSpec(ChartSpec spec) {
    m_charts.put(spec.getID(), spec);
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

  public void addGate(AbstractGate gate, String dataFrameID) {
    if (!modelPlans.containsKey(OVERVEIW_PLAN_KEY)){
      modelPlans.put(dataFrameID, new ArrayList<String>());
    }
    List<String> masterPlan = modelPlans.get(OVERVEIW_PLAN_KEY);
    masterPlan.add(gate.getID());
    modelGates.put(gate.getID(), gate);
  }

  public List<AbstractGate> findGates(String dataFrameID) {
    List<String> plan = modelPlans.get(OVERVEIW_PLAN_KEY);

    if (plan!=null){
      List<AbstractGate> gates = plan.stream()
          .map(gateID -> modelGates.get(gateID))
          .collect(Collectors.toList());
      return gates;
    } else {
      List<AbstractGate> emptyList = new ArrayList<AbstractGate>();
      return emptyList;
    }
  }

  public void deleteGate(String id) {
    modelGates.remove(id);
    modelPlans.get(OVERVEIW_PLAN_KEY).remove(id);
  }
}
