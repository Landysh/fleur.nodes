package io.landysh.inflor.java.knime.nodes.CellCycle;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ModelCellCycle" Node.
 * Uses watson pragmatic modeling <citation> to predict the number of cells in each stage of the cell cycle.  
 *
 * @author Aaron Hart
 */
public class LearnCellCycleNodeFactory 
        extends NodeFactory<LearnCellCyclyNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LearnCellCyclyNodeModel createNodeModel() {
        return new LearnCellCyclyNodeModel();
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
    public NodeView<LearnCellCyclyNodeModel> createNodeView(final int viewIndex,
            final LearnCellCyclyNodeModel nodeModel) {
        return new LearnCellCycleNodeView(nodeModel);
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
        return new LearnCellCycleNodeDialog();
    }

}

