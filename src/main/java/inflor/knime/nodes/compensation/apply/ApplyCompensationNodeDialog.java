package inflor.knime.nodes.compensation.apply;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;

import inflor.knime.data.type.cell.fcs.FCSFrameCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "ApplyCompensation" Node. Attempts to apply a supplied
 * compensation matrix to a dataset.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ApplyCompensationNodeDialog extends DefaultNodeSettingsPane {

  private static final String LABEL_RETAIN_UNCOMPED = "Keep uncompensated parameters?";
  private static final String LABEL_SELECTED_COLUMN = "Selected FCS Column";
  private static final int FCS_INPUT_INDEX = 1;

  /**
   * New pane for configuring the ApplyCompensation node.
   */
  ApplyCompensationNodeDialog() {
    super();
    // The retain compensation option.
    SettingsModelBoolean retainedUncompSMB =
        new SettingsModelBoolean(ApplyCompensationNodeModel.KEY_RETAIN_UNCOMPED,
            ApplyCompensationNodeModel.DEFAULT_RETAIN_UNCOMPED);
    DialogComponentBoolean retainedUncompDCB =
        new DialogComponentBoolean(retainedUncompSMB, LABEL_RETAIN_UNCOMPED);
    addDialogComponent(retainedUncompDCB);

    // the fcs column filter chooser.
    SettingsModelColumnName columnNameSM =
        new SettingsModelColumnName(ApplyCompensationNodeModel.KEY_SELECTED_COLUMN,
            ApplyCompensationNodeModel.DEFAULT_SELECTED_COLUMN);
    DialogComponent fcsFrameDCCF = new DialogComponentColumnNameSelection(columnNameSM,
        LABEL_SELECTED_COLUMN, FCS_INPUT_INDEX, new FCSFrameCellColumnFilter());
    addDialogComponent(fcsFrameDCCF);
  }
}

