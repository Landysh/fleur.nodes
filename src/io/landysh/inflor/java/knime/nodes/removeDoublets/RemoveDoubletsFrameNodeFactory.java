package io.landysh.inflor.java.knime.nodes.removeDoublets;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FindSingletsFrame" Node.
 * 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeFactory 
        extends NodeFactory<RemoveDoubletsFrameNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RemoveDoubletsFrameNodeModel createNodeModel() {
        return new RemoveDoubletsFrameNodeModel();
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
    public NodeView<RemoveDoubletsFrameNodeModel> createNodeView(final int viewIndex,
            final RemoveDoubletsFrameNodeModel nodeModel) {
        return new RemoveDoubletsFrameNodeView(nodeModel);
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
        return new RemoveDoubletsFrameNodeDialog();
    }

}

