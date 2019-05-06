package inflor.knime.nodes.gating;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import fleur.core.data.DomainObject;
import inflor.core.gates.Hierarchical;
import inflor.knime.core.NodeUtilities;

public class CreateGatesNodeSettings {
  
  private NodeLogger logger = NodeLogger.getLogger(getClass());

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private static final String NODE_BYTES_KEY = "Node Bytes";
  public static final String DEFAULT_SELECTED_COLUMN = "No Column Selected";

  private String selectedColumn = DEFAULT_SELECTED_COLUMN;
  private HashMap<String, Hierarchical> nodePool;
  
  public CreateGatesNodeSettings() {
    nodePool = new HashMap<>();
  }

  public void addNode(String fullPath, DomainObject newNode) {
    if (nodePool.containsKey(fullPath)){
      DomainObject oldObject = (DomainObject) nodePool.get(fullPath);
      newNode.setID(oldObject.getID());
    }
    nodePool.put(fullPath, (Hierarchical) newNode);
  }
  
  public void removeNode(String path) {
    if (nodePool.containsKey(path)){
      nodePool.remove(path);
    } else {
      logger.warn(path + " not found");
    }
  }
  
  public Hierarchical getNode(String id){
    Optional<Hierarchical> searchResults = nodePool
      .entrySet()
      .stream()
      .filter(e -> e.getValue().getID().equals(id))
      .map(Entry::getValue)
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
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
   selectedColumn = settings.getString(SELECTED_COLUMN_KEY);
   Map<String, Serializable> nodeSet = NodeUtilities.loadMap(settings, NODE_BYTES_KEY);
   nodePool = new HashMap<>();
   nodeSet
     .entrySet()
     .stream()
     .filter(entry -> entry.getValue() instanceof Hierarchical)
     .forEach(e -> nodePool.put(e.getKey(), (Hierarchical) e.getValue()));
   
   logger.info(nodePool.size() +  " tree nodes loaded."); 
  }

  public void setSelectedColumn(String newColumn) {
    selectedColumn = newColumn;
  }

  public String getSelectedColumn() {
    return selectedColumn;
  }

  public void validate(NodeSettingsRO settings) {/*TODO*/}

  public Map<String, Hierarchical> getNodes() {
    return nodePool;
  }
}
