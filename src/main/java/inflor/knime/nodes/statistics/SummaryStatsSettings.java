package inflor.knime.nodes.statistics;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import inflor.knime.core.NodeUtilities;

public class SummaryStatsSettings {

  private static final String SELECTED_COLUMN_KEY = "Selected Column";
  private String modelSelectedColumn;
  private List<StatSpec> stats;

  private static final String STATS_KEY = "Statistic Definitions";

  private static final NodeLogger logger = NodeLogger.getLogger(SummaryStatisticsNodeModel.class);


  public SummaryStatsSettings() {
    stats = new ArrayList<>();
  }

  public void save(NodeSettingsWO settings) {
    settings.addString(SELECTED_COLUMN_KEY, modelSelectedColumn);
    try {
      NodeUtilities.saveSerializable(settings, STATS_KEY, (Serializable) stats);
    } catch (IOException e) {
      logger.error(NodeUtilities.getSaveSerializableErrorMessage(), e);
    }
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    modelSelectedColumn = settings.getString(SELECTED_COLUMN_KEY);
    List<Serializable> serialStats = NodeUtilities.loadList(settings, STATS_KEY);
    stats = serialStats.stream().map(ser -> (StatSpec) ser).collect(Collectors.toList());
  }

  public String getSelectedColumn() {
    return modelSelectedColumn;
  }

  public void setSelectedColumn(String newValue) {
    modelSelectedColumn = newValue;
  }

  public void validate(NodeSettingsRO settings) {
    // TODO
  }

  public void addStatSpec(StatSpec statSpec) {
    if (statSpec != null && !stats.contains(statSpec)) {
      stats.add(statSpec);
    }
  }

  public List<StatSpec> getStatSpecs() {
    return stats;
  }

  public void removeStat(StatSpec spec) {
    stats.remove(spec);
  }
}
