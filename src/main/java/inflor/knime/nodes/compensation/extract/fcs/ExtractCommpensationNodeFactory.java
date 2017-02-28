package inflor.knime.nodes.compensation.extract.fcs;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ExtractCommpensation" Node.
 * 
 *
 * @author Aaron Hart
 */
public class ExtractCommpensationNodeFactory 
        extends NodeFactory<ExtractCommpensationNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtractCommpensationNodeModel createNodeModel() {
        return new ExtractCommpensationNodeModel();
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
    public NodeView<ExtractCommpensationNodeModel> createNodeView(final int viewIndex,
            final ExtractCommpensationNodeModel nodeModel) {
        return new ExtractCommpensationNodeView(nodeModel);
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
        return new ExtractCommpensationNodeDialog();
    }

}

