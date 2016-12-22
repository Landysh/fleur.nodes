package main.java.inflor.knime.nodes.createGates;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import main.java.inflor.core.gates.GateUtilities;
import main.java.inflor.core.gates.Hierarchical;
import main.java.inflor.knime.core.NodeUtilities;

public class CreateGatesNodeSettings {

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private static final String NODE_BYTES_KEY = "Node Bytes";
  private static final String SAMPLE_PLAN_KEY = "Sample plan";

  private String selectedColumn;
  private HashSet<Hierarchical> nodePool;
  private HashMap <String, List<String>> sampleSpecificPlans;
  
  
  public CreateGatesNodeSettings() {
    nodePool = new HashSet<Hierarchical>();
    sampleSpecificPlans = new HashMap <String, List<String>>();
  }

  public void addNode(Hierarchical newNode, String sourceID) {
    List<Hierarchical> existingNodes = nodePool
      .stream()
      .filter(node -> node.getID().equals(newNode.getID()))
      .collect(Collectors.toList()); 
    if (existingNodes.size()>0){
      existingNodes.forEach(node -> nodePool.remove(node));
    }
    nodePool.add(newNode);
    
    if (sampleSpecificPlans.containsKey(sourceID)){
      sampleSpecificPlans.get(sourceID).add(newNode.getID());
    } else {
      List<String> plan = new ArrayList<>();
      plan.add(newNode.getID());
      sampleSpecificPlans.put(sourceID, plan);
    }
  }
  
  public void removeNode(Hierarchical node) {
    nodePool.remove(node);
  }
  
  public Hierarchical getNode(String id){
    Optional<Hierarchical> searchResults = nodePool
      .stream()
      .filter(node -> node.getID().equals(id))
      .findAny();
    
    if (searchResults.isPresent()){
      return searchResults.get();
    } else {
      return null;
    }
  }
  
  public void save(NodeSettingsWO settings) throws IOException {
    settings.addString(SELECTED_COLUMN_KEY, selectedColumn);
    NodeUtilities.saveSerializable(settings, NODE_BYTES_KEY, nodePool);
    NodeUtilities.saveSerializable(settings, SAMPLE_PLAN_KEY, sampleSpecificPlans);
    settings.addStringArray(SAMPLE_PLAN_KEY, (sampleSpecificPlans.keySet().toArray(new String[sampleSpecificPlans.size()])));
    for (String key: sampleSpecificPlans.keySet()){
      List<String> plan = sampleSpecificPlans.get(key);
      String[] planKeys = plan.toArray(new String[plan.size()]);
      settings.addStringArray(key, planKeys);
    }
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
   selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
   HashSet<Serializable> nodeSet = NodeUtilities.loadSet(settings, NODE_BYTES_KEY);
   nodePool = new HashSet<Hierarchical>();
   nodeSet
     .stream()
     .filter(obj -> obj instanceof Hierarchical)
     .map(obj -> (Hierarchical) obj)
     .forEach(domainObjectg -> nodePool.add(domainObjectg));
    sampleSpecificPlans = new HashMap<String, List<String>>();
    String[] planKeys = settings.getStringArray(SAMPLE_PLAN_KEY);
    for (String key: planKeys){
      List<String> nodeIDs = Arrays.asList(settings.getStringArray(key));
      sampleSpecificPlans.put(key, nodeIDs);
    }
  }

  public void setSelectedColumn(String newColumn) {
    selectedColumn = newColumn;
  }

  public String getSelectedColumn() {
    return selectedColumn;
  }

  public void validate(NodeSettingsRO settings) {
    // TODO Auto-generated method stub
  }

  public List<Hierarchical> findNodes(String id) {
    String key =null;
    if (sampleSpecificPlans.containsKey(id)){
      key = id;
    } else {
      key = GateUtilities.SUMMARY_FRAME_ID;
    }
    
    List<Hierarchical> foundGates;
    if (sampleSpecificPlans.containsKey(key)){
      foundGates = sampleSpecificPlans
          .get(key)
          .stream()
          .map(nodeKey -> getNode(nodeKey))
          .collect(Collectors.toList()); 
    } else{
      foundGates = new ArrayList<Hierarchical>();
    }
    return foundGates;
  }
}
