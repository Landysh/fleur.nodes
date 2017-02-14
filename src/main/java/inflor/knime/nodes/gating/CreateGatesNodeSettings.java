package main.java.inflor.knime.nodes.gating;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
    nodePool = new HashSet<>();
    sampleSpecificPlans = new HashMap <>();
  }

  public void addNode(Hierarchical newNode, String sourceID) {
    List<Hierarchical> existingNodes = nodePool
      .stream()
      .filter(node -> node.getID().equals(newNode.getID()))
      .collect(Collectors.toList()); 
    if (!existingNodes.isEmpty()){
      existingNodes.forEach(nodePool::remove);//TODO: one stream, later.
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
    //TODO Ask bernd about this.
    NodeUtilities.saveSerializable(settings, NODE_BYTES_KEY, nodePool);
    NodeUtilities.saveSerializable(settings, SAMPLE_PLAN_KEY, sampleSpecificPlans);
    settings.addStringArray(SAMPLE_PLAN_KEY, sampleSpecificPlans
        .keySet()
        .toArray(new String[sampleSpecificPlans.size()]));
    for (Entry<String, List<String>> entry: sampleSpecificPlans.entrySet()){
      List<String> plan = entry.getValue();
      String[] planKeys = plan.toArray(new String[plan.size()]);
      settings.addStringArray(entry.getKey(), planKeys);
    }
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
   selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
   HashSet<Serializable> nodeSet = NodeUtilities.loadSet(settings, NODE_BYTES_KEY);
   nodePool = new HashSet<>();
   nodeSet
     .stream()
     .filter(obj -> obj instanceof Hierarchical)
     .map(obj -> (Hierarchical) obj)
     .forEach(nodePool::add);
    sampleSpecificPlans = new HashMap<>();
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

  public void validate(NodeSettingsRO settings) {/*TODO*/}

  public List<Hierarchical> findNodes(String id) {
    String key;
    if (sampleSpecificPlans.containsKey(id)){
      key = id;
    } else {
      key = GateUtilities.SUMMARY_FRAME_ID;
    }
    
    List<Hierarchical> foundNodes;
    if (sampleSpecificPlans.containsKey(key)){
      foundNodes = sampleSpecificPlans
          .get(key)
          .stream()
          .map(this::getNode)
          .collect(Collectors.toList()); 
    } else{
      foundNodes = new ArrayList<>();
    }
    return foundNodes;
  }
}
