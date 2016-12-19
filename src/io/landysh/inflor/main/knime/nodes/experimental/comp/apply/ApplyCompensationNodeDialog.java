package io.landysh.inflor.main.knime.nodes.experimental.comp.apply;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "ApplyCompensation" Node.
 * Attempts to apply a supplied compensation matrix to a dataset.  
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ApplyCompensationNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ApplyCompensation node.
     */
    protected ApplyCompensationNodeDialog() {

    }
}

