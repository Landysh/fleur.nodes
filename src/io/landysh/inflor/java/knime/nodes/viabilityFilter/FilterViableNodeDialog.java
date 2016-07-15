package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.core.viability.ViabilityFilterSettingsModel;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "FilterViable" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class FilterViableNodeDialog extends DefaultNodeSettingsPane {

	private ViabilityFilterSettingsModel m_settings = new ViabilityFilterSettingsModel();

	private static final String VIABILITY_LABEL = "Viability Parameter";
	public DialogComponentStringSelection viabilityColumnComponent;

	private static final String COLUMN_LABEL = "FCS Column";
	public DialogComponentColumnNameSelection columnSelectionComponent;

	protected FilterViableNodeDialog() {
		super();
		ArrayList<String> defaultChoices = new ArrayList<String>();
		defaultChoices.add("None");

		// Input column selector
		columnSelectionComponent = new DialogComponentColumnNameSelection(m_settings.getSelectedColumnSettingsModel(),
				COLUMN_LABEL, 0, new ColumnStoreCellColumnFilter());
		addDialogComponent(columnSelectionComponent);

		// Area Selector
		viabilityColumnComponent = new DialogComponentStringSelection(m_settings.getViabilityColumnSettingsModel(),
				VIABILITY_LABEL, defaultChoices);
		addDialogComponent(viabilityColumnComponent);

	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		try {
			m_settings.loadSettingsFrom(settings);
		} catch (Exception e) {
			throw new NotConfigurableException("Unable to load settings.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		DataTableSpec spec = specs[0];
		String name = m_settings.getSelectedColumnSettingsModel().getStringValue();

		DataColumnProperties properties = spec.getColumnSpec(name).getProperties();
		Enumeration<String> keys = properties.properties();
		Hashtable<String, String> keywords = new Hashtable<String, String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = properties.getProperty(key);
			keywords.put(key, value);
		}

		String[] vectorNames = FCSUtils.parseParameterList(keywords);
		ArrayList<String> areaChoices = new ArrayList<String>(Arrays.asList(vectorNames));

		viabilityColumnComponent.replaceListItems(areaChoices, areaChoices.get(0));
	}
}