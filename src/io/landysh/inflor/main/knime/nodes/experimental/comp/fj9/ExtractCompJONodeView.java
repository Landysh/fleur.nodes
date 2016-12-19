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
package io.landysh.inflor.main.knime.nodes.experimental.comp.fj9;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "ExtractCompJO" Node.
 * Extract a compenation matrix from a text file generated with FlowJo for Mac. This has only been tested with exports from version 9 of FlowJo.
 *
 * @author Aaron Hart
 */
public class ExtractCompJONodeView extends NodeView<ExtractCompJONodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ExtractCompJONodeModel})
     */
    protected ExtractCompJONodeView(final ExtractCompJONodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        ExtractCompJONodeModel nodeModel = 
            (ExtractCompJONodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

