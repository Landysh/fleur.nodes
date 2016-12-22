package main.java.inflor.knime.nodes.compensation.apply;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "ApplyCompensation" Node.
 * Attempts to apply a supplied compensation matrix to a dataset.  
 *
 * @author Aaron Hart
 */
public class ApplyCompensationNodeView extends NodeView<ApplyCompensationNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ApplyCompensationNodeModel})
     */
    protected ApplyCompensationNodeView(final ApplyCompensationNodeModel nodeModel) {
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

