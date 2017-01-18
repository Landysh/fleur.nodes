package main.java.inflor.knime.nodes.fcs.read.set;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ReadFCSSet" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class ReadFCSSetNodeDialog extends DefaultNodeSettingsPane {

  /**
   * New pane for configuring ReadFCSSet node dialog. This is just a suggestion to demonstrate
   * possible default dialog components.
   */
  protected ReadFCSSetNodeDialog() {
    super();
    // Path to files.
    SettingsModelString pathModel =
        new SettingsModelString(ReadFCSSetNodeModel.CFGKEY_PATH, ReadFCSSetNodeModel.DEFAULT_PATH);
    DialogComponentFileChooser pathComponent = new DialogComponentFileChooser(pathModel,
        "ReadFCSSetPathHistory", JFileChooser.OPEN_DIALOG, true);
    addDialogComponent(pathComponent);
  }
}
