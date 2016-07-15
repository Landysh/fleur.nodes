package io.landysh.inflor.java.knime.nodes.BHTSNE;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "BHTSNE" Node.
 * 
 *
 * @author Landysh Co.
 */
public class BHTSNENodeFactory extends NodeFactory<BHTSNENodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BHTSNENodeModel createNodeModel() {
		return new BHTSNENodeModel();
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
	public NodeView<BHTSNENodeModel> createNodeView(final int viewIndex, final BHTSNENodeModel nodeModel) {
		return new BHTSNENodeView(nodeModel);
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
		return new BHTSNENodeDialog();
	}

}
