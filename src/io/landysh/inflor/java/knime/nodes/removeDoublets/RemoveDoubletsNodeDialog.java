package io.landysh.inflor.java.knime.nodes.removeDoublets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import io.landysh.inflor.java.core.singlets.PuleProperties;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "RemoveDoublets" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class RemoveDoubletsNodeDialog extends DefaultNodeSettingsPane {

	private RemoveDoubletsSettingsModel m_settings = new RemoveDoubletsSettingsModel();

	public DialogComponentStringSelection areaComponent;
	public DialogComponentStringSelection heightComponent;
	public DialogComponentColumnNameSelection columnSelectionComponent;

	private static final String AREA_LABEL = "Area Parameter";
	private static final String HEIGHT_LABEL = "Height Parameter";
	private static final String COLUMN_LABEL = "Input Column";

	protected RemoveDoubletsNodeDialog() {
		super();
		ArrayList<String> defaultChoices = new ArrayList<String>();
		defaultChoices.add("None");

		// Input column selector
		columnSelectionComponent = new DialogComponentColumnNameSelection(m_settings.getSelectedColumnSettingsModel(),
				COLUMN_LABEL, 0, new ColumnStoreCellColumnFilter());
		addDialogComponent(columnSelectionComponent);

		// Area Selector
		areaComponent = new DialogComponentStringSelection(m_settings.getAreaColumnSettingsModel(), AREA_LABEL,
				defaultChoices);
		addDialogComponent(areaComponent);

		// height Selector
		heightComponent = new DialogComponentStringSelection(m_settings.getHeightColumnSettingsModel(), HEIGHT_LABEL,
				defaultChoices);
		addDialogComponent(heightComponent);

	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		try {
			m_settings.load(settings);
		} catch (Exception e) {
			throw new NotConfigurableException("Unable to load settings.");
		}
	}

	// protected void loadSettingsFrom(NodeSettingsRO settings,
	// BufferedDataTable[] input)
	// throws NotConfigurableException {
	//
	// BufferedDataTable table = input[0];
	// table.getSpec().getColumnSpec("foo").getProperties().
	// for (DataRow row: table){
	// for (int i=0;i<row.getNumCells();i++){
	//
	// }
	// }
	// //TODO we are here.
	// String[] vectorNames = null;
	// SingletsModel model = new SingletsModel(vectorNames);
	//
	// ArrayList<String> areaChoices = model.findColumns(vectorNames,
	// PuleProperties.AREA);
	// ArrayList<String> heightChoices = model.findColumns(vectorNames,
	// PuleProperties.HEIGHT);
	//
	// areaComponent.replaceListItems(areaChoices, areaChoices.get(0));
	// heightComponent.replaceListItems(heightChoices, heightChoices.get(0));
	// try {
	// m_settings.load(settings);
	// } catch (Exception e) {
	// throw new NotConfigurableException("Unable to load settings.");
	// }
	// }

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
		SingletsModel model = new SingletsModel(vectorNames);

		ArrayList<String> areaChoices = model.findColumns(vectorNames, PuleProperties.AREA);
		ArrayList<String> heightChoices = model.findColumns(vectorNames, PuleProperties.HEIGHT);

		areaComponent.replaceListItems(areaChoices, areaChoices.get(0));
		heightComponent.replaceListItems(heightChoices, heightChoices.get(0));

	}

}
