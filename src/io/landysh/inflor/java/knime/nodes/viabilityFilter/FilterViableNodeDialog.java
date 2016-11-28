package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.knime.dataTypes.FCSFrameCell.FCSFrameCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "FilterViable" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class FilterViableNodeDialog extends DefaultNodeSettingsPane {

  private static final String VIABILITY_LABEL = "Viability Parameter";

  private static final String COLUMN_LABEL = "FCS Column";
  private final ViabilityFilterSettingsModel m_settings = new ViabilityFilterSettingsModel();

  public DialogComponentStringSelection viabilityColumnComponent;
  public DialogComponentColumnNameSelection columnSelectionComponent;

  protected FilterViableNodeDialog() {
    super();
    final ArrayList<String> defaultChoices = new ArrayList<String>();
    defaultChoices.add("None");

    // Input column selector
    columnSelectionComponent =
        new DialogComponentColumnNameSelection(m_settings.getSelectedColumnSettingsModel(),
            COLUMN_LABEL, 0, new FCSFrameCellColumnFilter());
    addDialogComponent(columnSelectionComponent);

    // Area Selector
    viabilityColumnComponent = new DialogComponentStringSelection(
        m_settings.getViabilityColumnSettingsModel(), VIABILITY_LABEL, defaultChoices);
    addDialogComponent(viabilityColumnComponent);

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

    final String[] vectorNames = FCSUtilities.parseDimensionList(keywords);
    final ArrayList<String> areaChoices = new ArrayList<String>(Arrays.asList(vectorNames));

    viabilityColumnComponent.replaceListItems(areaChoices, areaChoices.get(0));
  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    try {
      m_settings.loadSettingsFrom(settings);
    } catch (final Exception e) {
      throw new NotConfigurableException("Unable to load settings.");
    }
  }
}
