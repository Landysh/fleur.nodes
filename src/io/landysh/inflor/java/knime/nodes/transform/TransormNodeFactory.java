package io.landysh.inflor.java.knime.nodes.transform;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Transorm" Node.
 * 
 *
 * @author Aaron Hart
 */
public class TransormNodeFactory extends NodeFactory<TransformNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public TransformNodeModel createNodeModel() {
    return new TransformNodeModel();
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
  public NodeView<TransformNodeModel> createNodeView(final int viewIndex,
      final TransformNodeModel nodeModel) {
    return new TransormNodeView(nodeModel);
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
    return new TransormNodeDialog();
  }

}

