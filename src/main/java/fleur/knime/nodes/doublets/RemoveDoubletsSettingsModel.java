package fleur.knime.nodes.doublets;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class RemoveDoubletsSettingsModel {

  // Area parameter
  static final String CFGKEY_AreaColumn = "Area Column";
  static final String DEFAULT_AreaColumn = null;
  // Height parameter
  static final String CFGKEY_HeightColumn = "Height Column";

  static final String DEFAULT_HeightColumn = null;
  // Selected column from input table upon which to do the discrimination.
  static final String CFGKEY_FCSColumn = "FCS Column";
  static final String DEFAULT_FCSColumn = null;

  private final SettingsModelString m_AreaColumn =
      new SettingsModelString(CFGKEY_AreaColumn, DEFAULT_AreaColumn);
  private final SettingsModelString m_HeightColumn =
      new SettingsModelString(CFGKEY_HeightColumn, DEFAULT_HeightColumn);
  private final SettingsModelString m_FCSColumn =
      new SettingsModelString(CFGKEY_FCSColumn, DEFAULT_FCSColumn);

  public SettingsModelString getAreaColumnSettingsModel() {
    return m_AreaColumn;
  }

  public SettingsModelString getHeightColumnSettingsModel() {
    return m_HeightColumn;
  }

  public SettingsModelString getSelectedColumnSettingsModel() {
    return m_FCSColumn;
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    m_HeightColumn.loadSettingsFrom(settings);
    m_AreaColumn.loadSettingsFrom(settings);
    m_FCSColumn.loadSettingsFrom(settings);
  }

  public void save(NodeSettingsWO settings) {
    m_HeightColumn.saveSettingsTo(settings);
    m_AreaColumn.saveSettingsTo(settings);
    m_FCSColumn.saveSettingsTo(settings);
  }

  public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
    m_HeightColumn.validateSettings(settings);
    m_AreaColumn.validateSettings(settings);
    m_FCSColumn.validateSettings(settings);
  }
}
