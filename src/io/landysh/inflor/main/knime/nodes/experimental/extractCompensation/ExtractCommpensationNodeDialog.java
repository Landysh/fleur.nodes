package io.landysh.inflor.main.knime.nodes.experimental.extractCompensation;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ExtractCommpensation" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ExtractCommpensationNodeDialog extends DefaultNodeSettingsPane {

  /**
   * New pane for configuring FCSReader node dialog.
   */
  public ExtractCommpensationNodeDialog() {
    super();

    addDialogComponent(new DialogComponentFileChooser(
        new SettingsModelString(ExtractCommpensationNodeModel.CFGKEY_FileLocation,
            ExtractCommpensationNodeModel.DEFAULT_FileLocation),
        "foo", "fcs"));
  }
}

