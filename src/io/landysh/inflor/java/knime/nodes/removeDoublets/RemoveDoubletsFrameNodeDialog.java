package io.landysh.inflor.java.knime.nodes.removeDoublets;

import java.util.ArrayList;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import io.landysh.inflor.java.core.singlets.PuleProperties;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;

/**
 * <code>NodeDialog</code> for the "FindSingletsFrame" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeDialog extends DefaultNodeSettingsPane {

	/**
     * New pane for configuring the FindSingletsFrame node.
     */
    
	private static final String AREA_LABEL   = "Area Parameter";
	private static final String HEIGHT_LABEL = "Height Parameter";
        
    public DialogComponentStringSelection areaComponent;
    public DialogComponentStringSelection heightComponent;

    
	protected RemoveDoubletsFrameNodeDialog() {
		super();
	    ArrayList<String> defaultChoices = new ArrayList<String>();
		defaultChoices.add("None");		
		//Area Selector
		SettingsModelString m_areaStringSetting = new SettingsModelString(
				RemoveDoubletsFrameNodeModel.CFGKEY_AreaColumn, 
				RemoveDoubletsFrameNodeModel.DEFAULT_AreaColumn
				);
		areaComponent = new DialogComponentStringSelection(
				m_areaStringSetting, 
				AREA_LABEL, 
				defaultChoices
				);
		addDialogComponent(areaComponent);
				
		//height Selector
		SettingsModelString m_heightStringSetting = new SettingsModelString(
				RemoveDoubletsFrameNodeModel.CFGKEY_HeightColumn, 
				RemoveDoubletsFrameNodeModel.DEFAULT_HeightColumn
				);
		heightComponent = new DialogComponentStringSelection(
				m_heightStringSetting, 
				HEIGHT_LABEL, 
				defaultChoices
				);
		addDialogComponent(heightComponent);
		
	}

    /** {@inheritDoc} */
    @Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        ColumnStorePortSpec spec = (ColumnStorePortSpec) specs[0];
		String[] vectorNames = spec.columnNames;
        SingletsModel model = new SingletsModel(vectorNames);

        ArrayList<String> areaChoices = model.findColumns(vectorNames, PuleProperties.AREA);
		ArrayList<String> heightChoices = model.findColumns(vectorNames, PuleProperties.HEIGHT);
		
		areaComponent.replaceListItems(areaChoices, areaChoices.get(0));
		heightComponent.replaceListItems(heightChoices, heightChoices.get(0));
    } 
}