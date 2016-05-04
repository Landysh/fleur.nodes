package io.landysh.inflor.java.knime.nodes.findSinglets;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "RemoveDoublets" Node.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class FindSingletsNodeDialog extends DataAwareNodeDialogPane {

    /**
     * New pane for configuring the RemoveDoublets node.
     */
    protected FindSingletsNodeDialog() {
    	
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
}

