package fleur.knime.nodes.compensation.apply;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ApplyCompensation" Node. Attempts to apply a supplied
 * compensation matrix to a dataset.
 *
 * @author Aaron Hart
 */
public class ApplyCompensationNodeFactory extends NodeFactory<ApplyCompensationNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ApplyCompensationNodeModel createNodeModel() {
    return new ApplyCompensationNodeModel();
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
  public NodeView<ApplyCompensationNodeModel> createNodeView(final int viewIndex,
      final ApplyCompensationNodeModel nodeModel) {
    return new ApplyCompensationNodeView(nodeModel);
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
    return new ApplyCompensationNodeDialog();
  }

}

