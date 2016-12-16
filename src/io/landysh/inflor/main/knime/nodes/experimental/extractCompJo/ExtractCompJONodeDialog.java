/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 */
package io.landysh.inflor.main.knime.nodes.experimental.extractCompJo;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ExtractCompJO" Node.
 * Extract a compenation matrix from a text file generated with FlowJo for Mac. This has only been tested with exports from version 9 of FlowJo.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class ExtractCompJONodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring ExtractCompJO node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected ExtractCompJONodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentFileChooser(
            new SettingsModelString(ExtractCompJoSettings.KEY_FILEPATH,
                ExtractCompJoSettings.DEFAULT_FILEPATH),"compMatrixChooser", "mtx|txt"));
                    
    }
}

