package io.landysh.inflor.java.knime.nodes.Singlets;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RemoveDoublets" Node.
 * Attempts to identify and compare pulse shape parameters in order to remove aggregated particles. 
 *
 * @author Aaron Hart
 */
public class FindSingletsNodeFactory 
        extends NodeFactory<FindSingletsNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FindSingletsNodeModel createNodeModel() {
        return new FindSingletsNodeModel();
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
    public NodeView<FindSingletsNodeModel> createNodeView(final int viewIndex,
            final FindSingletsNodeModel nodeModel) {
        return new FindSingletsNodeView(nodeModel);
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
        return new FindSingletsNodeDialog();
    }

}

