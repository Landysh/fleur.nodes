package io.landysh.inflor.main.knime.nodes.compensate;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Compensate" Node. Will extract a compensation matrix from am
 * FCS file and apply it to a group of files
 *
 * @author Aaron Hart
 */
public class CompensateNodeFactory extends NodeFactory<CompensateNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public CompensateNodeModel createNodeModel() {
    return new CompensateNodeModel();
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
  public NodeView<CompensateNodeModel> createNodeView(final int viewIndex,
      final CompensateNodeModel nodeModel) {
    return new CompensateNodeView(nodeModel);
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
    return new CompensateNodeDialog();
  }

}

