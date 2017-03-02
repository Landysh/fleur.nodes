package inflor.knime.nodes.bhtsne.column;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: nhttps://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNEColumnNodeFactory extends NodeFactory<TSNEColumnNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new TSNEColumnNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TSNEColumnNodeModel createNodeModel() {
    return new TSNEColumnNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<TSNEColumnNodeModel> createNodeView(final int viewIndex,
      final TSNEColumnNodeModel nodeModel) {
    return new TSNEColumnNodeView(nodeModel);
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
