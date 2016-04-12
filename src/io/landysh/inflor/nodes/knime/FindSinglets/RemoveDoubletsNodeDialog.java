package io.landysh.inflor.nodes.knime.FindSinglets;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "RemoveDoublets" Node.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class RemoveDoubletsNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the RemoveDoublets node.
     */
    protected RemoveDoubletsNodeDialog() {

    }
}

