package fleur.knime.nodes.compensation.calculate;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CalculateCompensation" Node.
 * This node attempts to construct a compensation matrix automatically using heuristics to estimate sample roles and Theil-Sen estimation to calculate individual spillover values. 
 *
 * @author Aaron Hart
 */
public class CalculateCompensationNodeFactory 
        extends NodeFactory<CalculateCompensationNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CalculateCompensationNodeModel createNodeModel() {
        return new CalculateCompensationNodeModel();
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
    public NodeView<CalculateCompensationNodeModel> createNodeView(final int viewIndex,
            final CalculateCompensationNodeModel nodeModel) {
        return new CalculateCompensationNodeView(nodeModel);
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
        return new CalculateCompensationNodeDialog();
    }

}

