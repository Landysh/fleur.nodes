package main.java.inflor.knime.nodes.compensation.convert;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "ConvertMatrix" Node.
 * Converts a compensation matrix to a standard KNIME Table.  
 *
 * @author Aaron Hart
 */
public class ConvertMatrixNodeView extends NodeView<ConvertMatrixNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link ConvertMatrixNodeModel})
     */
    protected ConvertMatrixNodeView(final ConvertMatrixNodeModel nodeModel) {
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

