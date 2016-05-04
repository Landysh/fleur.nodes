package io.landysh.inflor.java.knime.nodes.findSinglets;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "RemoveDoublets" Node.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * @author Aaron Hart
 */
public class FindSingletsNodeView extends NodeView<FindSingletsNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link FindSingletsNodeModel})
     */
    protected FindSingletsNodeView(final FindSingletsNodeModel nodeModel) {
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

