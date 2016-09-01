package io.landysh.inflor.knime.nodes.summaryStats;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

public class SummaryStatsSettings {

	static final String KEY_RESPONSE_MARKERS = "ResponseMarkers";
	static final String[] RESPONSE_MARKERS_DEFAULT = null;
	static final String SELECTED_STATS_KEY = "SelectedStats";

	static final String[] SELECTED_STATS_DEFAULT = null;
	// Selected column from input table upon which to do the discrimination.
	static final String FCS_COLUMN_KEY = "FCSColumn";
	static final String FCS_COLUMN_DEFAULT = null;

	private final SettingsModelStringArray m_responseMarkers = new SettingsModelStringArray(KEY_RESPONSE_MARKERS,
			RESPONSE_MARKERS_DEFAULT);
	private final SettingsModelStringArray m_selectedStats = new SettingsModelStringArray(SELECTED_STATS_KEY,
			SELECTED_STATS_DEFAULT);
	private final SettingsModelString m_FCSColumn = new SettingsModelString(FCS_COLUMN_KEY, FCS_COLUMN_DEFAULT);

	public SettingsModelStringArray getResponseMarkersModel() {
		return m_responseMarkers;
	}

	public SettingsModelString getSelectedColumnSettingsModel() {
		return m_FCSColumn;
	}

	public SettingsModelStringArray getSelectedStatsModel() {
		return m_selectedStats;
	}

	public void load(NodeSettingsRO settings) throws InvalidSettingsException {
		m_selectedStats.loadSettingsFrom(settings);
		m_responseMarkers.loadSettingsFrom(settings);
		m_FCSColumn.loadSettingsFrom(settings);
	}

	public void save(NodeSettingsWO settings) {
		m_selectedStats.saveSettingsTo(settings);
		m_responseMarkers.saveSettingsTo(settings);
		m_FCSColumn.saveSettingsTo(settings);
	}

	public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
		m_selectedStats.validateSettings(settings);
		m_responseMarkers.validateSettings(settings);
		m_FCSColumn.validateSettings(settings);
	}
}
// EOF