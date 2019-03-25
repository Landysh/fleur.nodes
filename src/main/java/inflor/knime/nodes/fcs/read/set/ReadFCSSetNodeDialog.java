package inflor.knime.nodes.fcs.read.set;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
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

  // Paths
  static final String HISTORY_ID = "ReadFCSSetPathHistory";
  final SettingsModelString mPath;
  final DialogComponentFileChooser vPath;

  
  // Compensate on read
  static final String COMP_LABEL = "Apply Compensation";
  final SettingsModelBoolean mCompOnRead;
  final DialogComponentBoolean vCompOnRead;

  
  protected ReadFCSSetNodeDialog() {
    
    super();
    // Path to files.
    mPath = new SettingsModelString(ReadFCSSetNodeModel.KEY_PATH, ReadFCSSetNodeModel.DEFAULT_PATH);
    vPath = new DialogComponentFileChooser(mPath, HISTORY_ID, JFileChooser.OPEN_DIALOG, true);
    addDialogComponent(vPath);
    
    // Compensate on read.
    mCompOnRead = new SettingsModelBoolean(ReadFCSSetNodeModel.KEY_COMP, ReadFCSSetNodeModel.DEFAULT_COMP);
    vCompOnRead = new DialogComponentBoolean(mCompOnRead, COMP_LABEL);
    addDialogComponent(vCompOnRead);

  }
}
