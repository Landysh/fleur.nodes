package main.java.inflor.knime.nodes.utility.extractsubset;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "ExtractTrainingSet" Node.
 * Extracts data from an FCS frame column to a standard KNIME Table. Data may be transformed and gating information included for downstream ML applications.
 *
 * @author Aaron Hart
 */
public class ExtractTrainingSetNodeView extends NodeView<ExtractTrainingSetNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ExtractTrainingSetNodeModel})
     */
    protected ExtractTrainingSetNodeView(final ExtractTrainingSetNodeModel nodeModel) {
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
        ExtractTrainingSetNodeModel nodeModel = 
            (ExtractTrainingSetNodeModel)getNodeModel();
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

