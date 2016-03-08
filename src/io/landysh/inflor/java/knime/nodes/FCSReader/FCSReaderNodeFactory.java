package io.landysh.inflor.java.knime.nodes.FCSReader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FCSReader" Node. It will do stuff
 *
 * @author Landysh
 */
public class FCSReaderNodeFactory extends NodeFactory<FCSReaderNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FCSReaderNodeModel createNodeModel() {
		return new FCSReaderNodeModel();
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
	public NodeView<FCSReaderNodeModel> createNodeView(final int viewIndex, final FCSReaderNodeModel nodeModel) {
		return new FCSReaderNodeView(nodeModel);
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
