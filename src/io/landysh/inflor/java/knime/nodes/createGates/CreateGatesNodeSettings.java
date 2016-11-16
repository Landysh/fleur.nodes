package io.landysh.inflor.java.knime.nodes.createGates;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import io.landysh.inflor.java.core.gates.Hierarchical;
import io.landysh.inflor.java.knime.core.NodeUtilities;

public class CreateGatesNodeSettings {

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private static final String NODE_BYTES_KEY = "Node Bytes";

  private String selectedColumn;
  private HashSet<Hierarchical> nodePool;
  
  public CreateGatesNodeSettings() {
    nodePool = new HashSet<Hierarchical>();
  }

  public void addNode(Hierarchical node) {
    nodePool.add(node);
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

  public HashSet<Hierarchical> getNodePool() {
    return nodePool;
  }
}
