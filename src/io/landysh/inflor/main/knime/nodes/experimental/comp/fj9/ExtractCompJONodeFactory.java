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

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ExtractCompJO" Node.
 * Extract a compenation matrix from a text file generated with FlowJo for Mac. This has only been tested with exports from version 9 of FlowJo.
 *
 * @author Aaron Hart
 */
public class ExtractCompJONodeFactory 
        extends NodeFactory<ExtractCompJONodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtractCompJONodeModel createNodeModel() {
        return new ExtractCompJONodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ExtractCompJONodeModel> createNodeView(final int viewIndex,
            final ExtractCompJONodeModel nodeModel) {
        return new ExtractCompJONodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ExtractCompJONodeDialog();
    }

}

