package main.java.inflor.knime.nodes.utility.extractsubset;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import main.java.inflor.knime.data.type.cell.fcs.FCSFrameCellColumnFilter;

/**
 * <code>NodeDialog</code> for the "ExtractTrainingSet" Node.
 * Extracts data from an FCS frame column to a standard KNIME Table. Data may be transformed and gating information included for downstream ML applications.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ExtractTrainingSetNodeDialog extends DefaultNodeSettingsPane {

    private static final String LABEL_EVENTS_PER_FILE = "Events per file";
    private static final String LABEL_SELECTED_COLUMN = "Selected column";
    private static final int FCS_INPUT_INDEX = 0;

    /**
     * New pane for configuring ExtractTrainingSet node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected ExtractTrainingSetNodeDialog() {
        super();
        
        //the fcs column filter chooser.
        SettingsModelColumnName columnNameSM = new SettingsModelColumnName(ExtractTrainingSetNodeModel.KEY_SELECTED_COLUMN, 
            ExtractTrainingSetNodeModel.DEFAULT_COLUMN);
        DialogComponent fcsFrameDCCF = new DialogComponentColumnNameSelection(columnNameSM, 
                                                                              LABEL_SELECTED_COLUMN, 
                                                                              FCS_INPUT_INDEX, 
                                                                              new FCSFrameCellColumnFilter());
        addDialogComponent(fcsFrameDCCF);
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    ExtractTrainingSetNodeModel.KEY_PERFILE_EVENT_COUNT,
                    ExtractTrainingSetNodeModel.DEFAULT_COUNT,
                    ExtractTrainingSetNodeModel.MIN_COUNT, ExtractTrainingSetNodeModel.MAX_COUNT),
                    LABEL_EVENTS_PER_FILE, /*step*/ 1000, /*componentwidth*/ 10));
                    
    }
}

