package io.landysh.inflor.knime.nodes.summaryStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;

import io.landysh.inflor.java.core.utils.FCSUtils;

/**
 * <code>NodeDialog</code> for the "SummaryStatistics" Node. Extract basic summary statistics from a
 * set of FCS Files.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh
 */
public class SummaryStatisticsNodeDialog extends DefaultNodeSettingsPane {

  DialogComponentStringListSelection markerFilter;
  DialogComponentStringListSelection statFilter;

  String[] selectableSummaryStats;

  SummaryStatsSettings m_settings = new SummaryStatsSettings();

  /**
   * New pane for configuring the SummaryStatistics node.
   */
  protected SummaryStatisticsNodeDialog() {

    selectableSummaryStats = new String[SummaryStatTypes.values().length];
    for (int i = 0; i < selectableSummaryStats.length; i++) {
      selectableSummaryStats[i] = SummaryStatTypes.values()[i].label();
    }

    markerFilter = new DialogComponentStringListSelection(m_settings.getResponseMarkersModel(),
        "Response Markers", new String[] {});

    statFilter = new DialogComponentStringListSelection(m_settings.getSelectedStatsModel(),
        "Statistics", selectableSummaryStats);
  }

  /** {@inheritDoc} */
  @Override
  public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    final DataTableSpec spec = specs[0];
    final String name = m_settings.getSelectedColumnSettingsModel().getStringValue();

    final DataColumnProperties properties = spec.getColumnSpec(name).getProperties();
    final Enumeration<String> keys = properties.properties();
    final HashMap<String, String> keywords = new HashMap<String, String>();
    while (keys.hasMoreElements()) {
      final String key = keys.nextElement();
      final String value = properties.getProperty(key);
      keywords.put(key, value);
    }

    final String[] vectorNames = FCSUtils.parseDimensionList(keywords);
    final ArrayList<String> newItems = (ArrayList<String>) Arrays.asList(vectorNames);
    markerFilter.replaceListItems(newItems, (String) null);
  }
}
