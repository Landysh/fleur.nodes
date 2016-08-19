package io.landysh.inflor.java.knime.nodes.createGates;

import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsWO;

/**
 * <code>NodeDialog</code> for the "CreateGates" Node.
 * @author 
 */

public class CreateGatesNodeDialog extends DataAwareNodeDialogPane {

	GatingModelNodeSettings m_Settings;
    protected CreateGatesNodeDialog() {
        super();
        m_Settings = new GatingModelNodeSettings();
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		m_Settings.save(settings);
	}
}

