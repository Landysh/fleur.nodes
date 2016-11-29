package io.landysh.inflor.main.knime.nodes.polygonGate;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PolygonGate" Node.
 * Filter data using a simple polygon
 *
 * @author Aaron Hart
 */
public class PolygonGateNodeFactory 
        extends NodeFactory<PolygonGateNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PolygonGateNodeModel createNodeModel() {
        return new PolygonGateNodeModel();
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
    public NodeView<PolygonGateNodeModel> createNodeView(final int viewIndex,
            final PolygonGateNodeModel nodeModel) {
        return new PolygonGateNodeView(nodeModel);
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
        return new PolygonGateNodeDialog();
    }

}

