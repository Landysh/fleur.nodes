package io.landysh.inflor.java.knime.nodes.compensate;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "Compensate" Node.
 * Will extract a compensation matrix from am FCS file and apply it to a group of files
 *
 * @author Aaron Hart
 */
public class CompensateNodeView extends NodeView<CompensateNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link CompensateNodeModel})
     */
    protected CompensateNodeView(final CompensateNodeModel nodeModel) {
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
        CompensateNodeModel nodeModel = 
            (CompensateNodeModel)getNodeModel();
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

