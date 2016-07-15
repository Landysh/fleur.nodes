package io.landysh.inflor.java.knime.nodes.downsample;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;

/**
 * <code>NodeDialog</code> for the "Downsample" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Incorportated
 */
public class DownsampleNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring Downsample node dialog. This is just a
	 * suggestion to demonstrate possible default dialog components.
	 */
	protected DownsampleNodeDialog() {
		super();

		final SettingsModelInteger m_Size = new SettingsModelInteger(DownsampleNodeModel.CFGKEY_Size,
				DownsampleNodeModel.DEFAULT_Size);

		addDialogComponent(new DialogComponentNumber(m_Size, "Sample Size", 1));
	}
}
