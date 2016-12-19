package io.landysh.inflor.main.knime.nodes.compensate.apply;
//package io.landysh.inflor.main.knime.nodes.experimental.comp.apply;
//
//import org.knime.core.node.InvalidSettingsException;
//import org.knime.core.node.NodeSettingsRO;
//import org.knime.core.node.NodeSettingsWO;
//
//public class ApplyCompensationSettings {
//
//  public static final String KEY_SELECTED_COLUMN = "Selected Column";
//  public static final String KEY_RETAIN_UNCOMPED = "Retain Uncomped Dimensions";
//  
//  public static final boolean DEFAULT_RETAIN_UNCOMPED = false;
//  public static final String DEFAULT_SELECTED_COLUMN = "Select...";
//  
//  private String mSelectedColumn= DEFAULT_SELECTED_COLUMN;
//  private boolean mRetainUncomped = DEFAULT_RETAIN_UNCOMPED;
//
//  ApplyCompensationSettings() {}
//
//  public void save(NodeSettingsWO settings) {
//    settings.addBoolean(KEY_RETAIN_UNCOMPED, mRetainUncomped);
//    settings.addString(KEY_SELECTED_COLUMN, mSelectedColumn);
//  }
//
//  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
//    mSelectedColumn = settings.getString(KEY_SELECTED_COLUMN);
//    mRetainUncomped = settings.getBoolean(KEY_RETAIN_UNCOMPED);
//  }
//
//  public String getSelectedColumn() {
//    return mSelectedColumn;
//  }
//
//  public void setSelectedColumn(String selectedItem) {
//    mSelectedColumn = selectedItem;
//  }
//
//  public void setRetainUncomped(boolean newValue) {
//    mRetainUncomped = newValue;
//  }
//
//  public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
//    if (settings.getString(KEY_SELECTED_COLUMN) == null){
//      throw new InvalidSettingsException("Selected Column is null");
//    } else if (settings.getString(KEY_RETAIN_UNCOMPED) ==null){
//      throw new InvalidSettingsException("Retain compensated dimensions is null");
//    }
//  }
//
//  public boolean getRetainUncomped() {
//    return mRetainUncomped;
//  }
//}
