package fleur.knime.nodes.compensation.convert;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ConvertMatrix" Node.
 * Converts a compensation matrix to a standard KNIME Table.  
 *
 * @author Aaron Hart
 */
public class ConvertMatrixNodeFactory 
        extends NodeFactory<ConvertMatrixNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ConvertMatrixNodeModel createNodeModel() {
        return new ConvertMatrixNodeModel();
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
    public NodeView<ConvertMatrixNodeModel> createNodeView(final int viewIndex,
            final ConvertMatrixNodeModel nodeModel) {
        return new ConvertMatrixNodeView(nodeModel);
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
        return new ConvertMatrixNodeDialog();
    }

}

