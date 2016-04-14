package io.landysh.inflor.java.knime.nodes.CellCycle;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "ModelCellCycle" Node.
 * Uses watson pragmatic modeling <citation> to predict the number of cells in each stage of the cell cycle.  
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class LearnCellCycleNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring ModelCellCycle node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected LearnCellCycleNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    LearnCellCyclyNodeModel.CFGKEY_COUNT,
                    LearnCellCyclyNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
                    
    }
}

