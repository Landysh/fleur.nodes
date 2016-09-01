package io.landysh.inflor.java.knime.nodes.removeDoublets;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RemoveDoublets" Node.
 * 
 *
 * @author Landysh Co.
 */
public class RemoveDoubletsNodeFactory extends NodeFactory<RemoveDoubletsNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new RemoveDoubletsNodeDialog();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RemoveDoubletsNodeModel createNodeModel() {
		return new RemoveDoubletsNodeModel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<RemoveDoubletsNodeModel> createNodeView(final int viewIndex,
			final RemoveDoubletsNodeModel nodeModel) {
		return new RemoveDoubletsNodeView(nodeModel);
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
	public boolean hasDialog() {
		return true;
	}

}
