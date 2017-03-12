package inflor.knime.nodes.doublets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import inflor.core.singlets.PuleProperties;
import inflor.core.singlets.SingletsModel;
import inflor.core.utils.FCSUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "RemoveDoublets" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class RemoveDoubletsNodeDialog extends DefaultNodeSettingsPane {

  private static final String AREA_LABEL = "Area Parameter";

  private static final String HEIGHT_LABEL = "Height Parameter";
  private static final String COLUMN_LABEL = "Input Column";
  private final RemoveDoubletsSettingsModel m_settings = new RemoveDoubletsSettingsModel();

  public DialogComponentStringSelection areaComponent;
  public DialogComponentStringSelection heightComponent;
  public DialogComponentColumnNameSelection columnSelectionComponent;

  protected RemoveDoubletsNodeDialog() {
    super();
    final ArrayList<String> defaultChoices = new ArrayList<String>();
    defaultChoices.add("None");

    // Input column selector
    columnSelectionComponent =
        new DialogComponentColumnNameSelection(m_settings.getSelectedColumnSettingsModel(),
            COLUMN_LABEL, 0, new FCSFrameCellColumnFilter());
    addDialogComponent(columnSelectionComponent);

    // Area Selector
    areaComponent = new DialogComponentStringSelection(m_settings.getAreaColumnSettingsModel(),
        AREA_LABEL, defaultChoices);
    addDialogComponent(areaComponent);

    // height Selector
    heightComponent = new DialogComponentStringSelection(m_settings.getHeightColumnSettingsModel(),
        HEIGHT_LABEL, defaultChoices);
    addDialogComponent(heightComponent);

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
    final SingletsModel model = new SingletsModel(vectorNames);

    final ArrayList<String> areaChoices = model.findColumns(vectorNames, PuleProperties.AREA);
    final ArrayList<String> heightChoices = model.findColumns(vectorNames, PuleProperties.HEIGHT);

    areaComponent.replaceListItems(areaChoices, null);
    heightComponent.replaceListItems(heightChoices, null);

  }

  @Override
  protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
      throws NotConfigurableException {
    try {
      m_settings.load(settings);
    } catch (final Exception e) {
      throw new NotConfigurableException("Unable to load settings.");
    }
  }

}
