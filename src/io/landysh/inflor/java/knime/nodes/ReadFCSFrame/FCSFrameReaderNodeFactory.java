package io.landysh.inflor.java.knime.nodes.readFCSFrame;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import io.landysh.inflor.java.knime.nodes.readFCS.FCSReaderNodeDialog;

/**
 * <code>NodeFactory</code> for the "FCSFrameReader" Node.
 * Reads a data file into an FCS Frame
 *
 * @author Aaron Hart
 */
public class FCSFrameReaderNodeFactory 
        extends NodeFactory<FCSFrameReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FCSFrameReaderNodeModel createNodeModel() {
        return new FCSFrameReaderNodeModel();
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
    public NodeView<FCSFrameReaderNodeModel> createNodeView(final int viewIndex,
            final FCSFrameReaderNodeModel nodeModel) {
        return new FCSFrameReaderNodeView(nodeModel);
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
        return new FCSReaderNodeDialog();
    }

}

