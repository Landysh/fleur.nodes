package io.landysh.inflor.java.knime.nodes.SNE;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "TSNE" Node.
 * Calculates a tSNE using library developed by Leif Jonsson: * nhttps://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNENodeView extends NodeView<TSNENodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link TSNENodeModel})
     */
    protected TSNENodeView(final TSNENodeModel nodeModel) {
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

