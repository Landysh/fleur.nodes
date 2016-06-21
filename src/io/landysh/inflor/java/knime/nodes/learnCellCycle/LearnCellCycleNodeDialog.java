package io.landysh.inflor.java.knime.nodes.learnCellCycle;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

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
        
                    
    }
}

