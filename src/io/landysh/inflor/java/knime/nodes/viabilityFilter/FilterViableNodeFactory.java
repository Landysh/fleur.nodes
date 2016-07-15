package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FilterViable" Node.
 * 
 *
 * @author Landysh Co.
 */
public class FilterViableNodeFactory extends NodeFactory<FilterViableNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FilterViableNodeModel createNodeModel() {
		return new FilterViableNodeModel();
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
	public NodeView<FilterViableNodeModel> createNodeView(final int viewIndex, final FilterViableNodeModel nodeModel) {
		return new FilterViableNodeView(nodeModel);
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
		return new FilterViableNodeDialog();
	}

}
