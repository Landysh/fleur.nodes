package fleur.knime.nodes.compensation.convert;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "ConvertMatrix" Node.
 * Converts a compensation matrix to a standard KNIME Table.  
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ConvertMatrixNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ConvertMatrix node.
     */
    protected ConvertMatrixNodeDialog() {

    }
}

