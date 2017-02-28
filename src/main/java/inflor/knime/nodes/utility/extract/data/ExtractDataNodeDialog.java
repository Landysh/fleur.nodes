package inflor.knime.nodes.utility.extract.data;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import inflor.knime.data.type.cell.fcs.FCSFrameCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "ExtractTrainingSet" Node. Extracts data from an FCS frame column
 * to a standard KNIME Table. Data may be transformed and gating information included for downstream
 * ML applications.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ExtractDataNodeDialog extends DefaultNodeSettingsPane {

  private static final String LABEL_EVENTS_PER_FILE = "Events per file";
  private static final String LABEL_SELECTED_COLUMN = "Selected column";
  private static final int FCS_INPUT_INDEX = 0;
  private static final String LABEL_TRANSFORM_DATA = "Transform data?";
  private static final String LABEL_DOWNSAMPLE_DATA = "Random subset?";
  private static final String DOWNSAMPLE_GROUP = "Downsample Options Group";

  /**
   * New pane for configuring ExtractTrainingSet node dialog. This is just a suggestion to
   * demonstrate possible default dialog components.
   */
  protected ExtractDataNodeDialog() {
    super();

    // the fcs column filter chooser.
    SettingsModelColumnName columnNameSM =
        new SettingsModelColumnName(ExtractDataNodeModel.KEY_SELECTED_COLUMN,
            ExtractDataNodeModel.DEFAULT_COLUMN);
    DialogComponent fcsFrameDCCF = new DialogComponentColumnNameSelection(columnNameSM,
        LABEL_SELECTED_COLUMN, FCS_INPUT_INDEX, new FCSFrameCellColumnFilter());
    addDialogComponent(fcsFrameDCCF);


    // Boolean for whether or not to transform when exporting the data
    DialogComponent transformBoolComp = new DialogComponentBoolean(
        new SettingsModelBoolean(ExtractDataNodeModel.KEY_TRANSFORM_DATA,
            ExtractDataNodeModel.DEFAULT_TRANSFORM),
        LABEL_TRANSFORM_DATA);
    addDialogComponent(transformBoolComp);


    createNewGroup(DOWNSAMPLE_GROUP);

    // Boolean for whether or not to downsample the data.
    DialogComponent downsampleBoolComp = new DialogComponentBoolean(
        new SettingsModelBoolean(ExtractDataNodeModel.KEY_DOWNSAMPLE_DATA,
            ExtractDataNodeModel.DEFAULT_DOWNSAMPLE),
        LABEL_DOWNSAMPLE_DATA);
    addDialogComponent(downsampleBoolComp);

    
    // event count per file
    addDialogComponent(new DialogComponentNumber(
        new SettingsModelIntegerBounded(ExtractDataNodeModel.KEY_PERFILE_EVENT_COUNT,
            ExtractDataNodeModel.DEFAULT_COUNT, ExtractDataNodeModel.MIN_COUNT,
            ExtractDataNodeModel.MAX_COUNT),
        LABEL_EVENTS_PER_FILE, /* step */ 1000, /* componentwidth */ 10));

    closeCurrentGroup();
  }
}

