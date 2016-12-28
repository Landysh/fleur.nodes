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
package main.java.inflor.knime.nodes.compensation.extractfj9mtx;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.node.NodeView;

import main.java.inflor.core.compensation.SpilloverCompensator;
import main.java.inflor.core.compensation.SpilloverRenderer;

/**
 * <code>NodeView</code> for the "ExtractCompJO" Node.
 * Extract a compenation matrix from a text file generated with FlowJo for Mac. This has only been tested with exports from version 9 of FlowJo.
 *
 * @author Aaron Hart
 */
public class ExtractCompJONodeView extends NodeView<ExtractCompJONodeModel> {

  private JPanel summary;

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ExtractCompJONodeModel})
     */
    protected ExtractCompJONodeView(final ExtractCompJONodeModel nodeModel) {
        super(nodeModel);
        summary = new JPanel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        ExtractCompJONodeModel nodeModel = getNodeModel();
        assert nodeModel != null;        
        if (nodeModel.isExecuted()){
          SpilloverCompensator compr = nodeModel.getCompensator();
          summary = SpilloverRenderer.toJPanel(compr);
          JScrollPane sp = new JScrollPane(summary);
          setComponent(sp);
          summary.revalidate();
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {/* noop*/}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {/* noop*/}

}

