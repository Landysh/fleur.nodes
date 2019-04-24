package fleur.knime.nodes.fcs.read.set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ReadFCSSetSettings {
  
  // Reader mode:

  public enum ReaderModes {
    Browser, Column
  }
  
  static final String KEY_MODE = "READER_MODE";
  static final String DEFAULT_MODE = ReaderModes.Browser.toString();
  static final SettingsModelString mMode = new SettingsModelString(KEY_MODE, DEFAULT_MODE.toString());
  
  // Folder containing FCS Files.
  static final String KEY_PATH = "PATH_TO_FILES";
  static final String DEFAULT_PATH = "None";
  private static final SettingsModelString mPath = new SettingsModelString(KEY_PATH, DEFAULT_PATH);
  
  // Selected column
  static final String KEY_SELECTED_COLUMN = "SELECTED_COLUMN";
  static final String DEFAULT_COLUMN = "Location";
  private static final SettingsModelString mColumn = new SettingsModelColumnName(KEY_SELECTED_COLUMN, DEFAULT_COLUMN);
  
  
  // Apply compensation
  static final String KEY_COMP = "APPLY_COMP";
  static final Boolean DEFAULT_COMP = true;
  private static final SettingsModelBoolean mComp = new SettingsModelBoolean(KEY_COMP, DEFAULT_COMP);
  
  public static String getPathValue() {
    return mPath.getStringValue();
  }

  public static String getColumnValue() {
    return mColumn.getStringValue();
  }

  public static Boolean getMComp() {
    return mComp.getBooleanValue();
  }
  
  public static String getMMode() {
    return mMode.getStringValue();
  }

  public static void save(NodeSettingsWO settings) {
    mPath.saveSettingsTo(settings);
    mComp.saveSettingsTo(settings);
    mColumn.saveSettingsTo(settings);
    mMode.saveSettingsTo(settings);
  }

  public static void validate(NodeSettingsRO settings) throws InvalidSettingsException {
    mPath.validateSettings(settings);
    mComp.validateSettings(settings);
    mColumn.validateSettings(settings);
    mMode.validateSettings(settings);
  }

  public static void load(NodeSettingsRO settings) throws InvalidSettingsException {
    mPath.loadSettingsFrom(settings);
    mComp.loadSettingsFrom(settings);
    mColumn.loadSettingsFrom(settings);
    mMode.loadSettingsFrom(settings);
  }
}
