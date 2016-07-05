package io.landysh.inflor.java.knime.nodes.BHTSNE;

import java.util.ArrayList;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

import io.landysh.inflor.java.core.singlets.PuleProperties;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;

/**
 * <code>NodeDialog</code> for the "BHTSNE" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class BHTSNENodeDialog extends DefaultNodeSettingsPane {
	
	BHTSNESettingsModel m_settings = new BHTSNESettingsModel();
    
	protected BHTSNENodeDialog() {
        super();
                    
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

    } 
}

