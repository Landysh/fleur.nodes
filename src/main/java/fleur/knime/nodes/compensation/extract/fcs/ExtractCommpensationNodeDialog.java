package fleur.knime.nodes.compensation.extract.fcs;

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
        new SettingsModelString(ExtractCommpensationNodeModel.KEY_FILE_LOCATION,
            ExtractCommpensationNodeModel.DEFAULT_FILE_LOCATION),
        "foo", "fcs"));
  }
}

