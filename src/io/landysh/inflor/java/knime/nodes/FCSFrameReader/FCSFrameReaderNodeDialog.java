package io.landysh.inflor.java.knime.nodes.FCSFrameReader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FCSReader" Node. It will do stuff
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class FCSFrameReaderNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring FCSReader node dialog. This is just a suggestion
	 * to demonstrate possible default dialog components.
	 */
	protected FCSFrameReaderNodeDialog() {
		super();

		addDialogComponent(new DialogComponentFileChooser(new SettingsModelString(
				FCSFrameReaderNodeModel.CFGKEY_FileLocation, FCSFrameReaderNodeModel.DEFAULT_FileLocation), "foo", "fcs"));

	}
}

