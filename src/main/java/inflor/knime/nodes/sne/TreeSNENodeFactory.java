 package inflor.knime.nodes.sne;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: https://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TreeSNENodeFactory extends NodeFactory<TreeSNENodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new TreeSNENodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TreeSNENodeModel createNodeModel() {
    return new TreeSNENodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<TreeSNENodeModel> createNodeView(final int viewIndex,
      final TreeSNENodeModel nodeModel) {
    return new TreeSNENodeView(nodeModel);
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
