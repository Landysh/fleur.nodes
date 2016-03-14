package io.landysh.inflor.java.knime.nodes.FCSFrameReader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "FCSFrameReader" Node.
 * This node reads an FCS file into an FCS Frame port
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class FCSFrameReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring FCSFrameReader node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected FCSFrameReaderNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    FCSFrameReaderNodeModel.CFGKEY_COUNT,
                    FCSFrameReaderNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
                    
    }
}

