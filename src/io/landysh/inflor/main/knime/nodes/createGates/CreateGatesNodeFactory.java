package io.landysh.inflor.main.knime.nodes.createGates;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CreateGates" Node.
 * 
 *
 * @author
 */
public class CreateGatesNodeFactory extends NodeFactory<CreateGatesNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new CreateGatesNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CreateGatesNodeModel createNodeModel() {
    return new CreateGatesNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<CreateGatesNodeModel> createNodeView(final int viewIndex,
      final CreateGatesNodeModel nodeModel) {
    return new CreateGatesNodeView(nodeModel);
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
