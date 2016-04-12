package io.landysh.inflor.nodes.knime.FindSinglets;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "RemoveDoublets" Node.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsNodeView extends NodeView<RemoveDoubletsNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link RemoveDoubletsNodeModel})
     */
    protected RemoveDoubletsNodeView(final RemoveDoubletsNodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

