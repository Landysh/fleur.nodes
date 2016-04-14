package io.landysh.inflor.java.knime.nodes.CellCycle;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "ModelCellCycle" Node.
 * Uses watson pragmatic modeling <citation> to predict the number of cells in each stage of the cell cycle.  
 *
 * @author Aaron Hart
 */
public class LearnCellCycleNodeView extends NodeView<LearnCellCyclyNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link LearnCellCyclyNodeModel})
     */
    protected LearnCellCycleNodeView(final LearnCellCyclyNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        LearnCellCyclyNodeModel nodeModel = 
            (LearnCellCyclyNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

