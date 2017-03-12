package inflor.knime.nodes.bhtsne.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: nhttps://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNENodeFactory extends NodeFactory<TSNENodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new TSNENodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TSNENodeModel createNodeModel() {
    return new TSNENodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<TSNENodeModel> createNodeView(final int viewIndex,
      final TSNENodeModel nodeModel) {
    return new TSNENodeView(nodeModel);
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
