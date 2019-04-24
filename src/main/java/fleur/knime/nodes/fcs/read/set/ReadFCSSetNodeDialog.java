package fleur.knime.nodes.fcs.read.set;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

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

  
  //Mode
  static final String MODE_LABEL = "Reader Mode";
  final SettingsModelString mMode;
  final DialogComponentButtonGroup vMode;
  
  // Paths
  static final String HISTORY_ID = "ReadFCSSetPathHistory";
  final SettingsModelString mPath;
  final DialogComponentFileChooser vPath;

  // Compensate on read
  static final String COMP_LABEL = "Apply Compensation";
  final SettingsModelBoolean mCompOnRead;
  final DialogComponentBoolean vCompOnRead;
  
  // Selected Column
  static final String COL_LABEL = "Selected Column";
  final SettingsModelColumnName mColumn;
  final DialogComponentColumnNameSelection vColumn;
  static final Boolean COL_SELECT_REQUIRED = false;
  static final Boolean COL_ALLOW_NONE = true;

  protected ReadFCSSetNodeDialog() {
    
    super();
    
    // Reader mode.
    mMode = new SettingsModelString(ReadFCSSetSettings.KEY_MODE, ReadFCSSetSettings.DEFAULT_MODE.toString());
    String[] vChoices = new String[ReadFCSSetSettings.ReaderModes.values().length];
    for (int i=0;i<vChoices.length;i++) {
      vChoices[i] = ReadFCSSetSettings.ReaderModes.values()[i].toString();
    }
    vMode = new DialogComponentButtonGroup(mMode, false, MODE_LABEL, vChoices);
    
    ChangeListener listener = new ChangeListener() {
      
      @Override
      public void stateChanged(ChangeEvent e) {
        String newMode = mMode.getStringValue();  
        
        if (newMode.equals(ReadFCSSetSettings.ReaderModes.Browser.toString())) {
            mColumn.setEnabled(false);
            mPath.setEnabled(true);
          } else if (newMode.equals(ReadFCSSetSettings.ReaderModes.Column.toString())) {
            mColumn.setEnabled(true);
            mPath.setEnabled(false);
          }
      }
    };
    mMode.addChangeListener(listener);
    addDialogComponent(vMode);

    // Path to files.
    mPath = new SettingsModelString(ReadFCSSetSettings.KEY_PATH, ReadFCSSetSettings.DEFAULT_PATH);
    vPath = new DialogComponentFileChooser(mPath, HISTORY_ID, JFileChooser.OPEN_DIALOG, true);
    addDialogComponent(vPath);
    
    // Selected Column (if present).
    mColumn  = new SettingsModelColumnName(ReadFCSSetSettings.KEY_SELECTED_COLUMN, ReadFCSSetSettings.DEFAULT_COLUMN);
    mColumn.setEnabled(false);
    ColumnFilter filter = new StringCellColumnFilter();
    vColumn  = new DialogComponentColumnNameSelection(mColumn, COL_LABEL, 0, COL_SELECT_REQUIRED, COL_ALLOW_NONE, filter);
    addDialogComponent(vColumn);
      
    // Compensate on read.
    mCompOnRead = new SettingsModelBoolean(ReadFCSSetSettings.KEY_COMP, ReadFCSSetSettings.DEFAULT_COMP);
    vCompOnRead = new DialogComponentBoolean(mCompOnRead, COMP_LABEL);
    addDialogComponent(vCompOnRead);    
    
  }
}
