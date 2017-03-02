package inflor.knime.nodes.bhtsne.column;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: nhttps://github.com/lejon/T-SNE-Java
 *
 * @author Aaron Hart
 */
public class TSNEColumnNodeView extends NodeView<TSNEColumnNodeModel> {

  /**
   * Creates a new view.
   * 
   * @param nodeModel The model (class: {@link TSNEColumnNodeModel})
   */
  protected TSNEColumnNodeView(final TSNEColumnNodeModel nodeModel) {
    super(nodeModel);
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void modelChanged() {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onClose() {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onOpen() {
    // TODO: generated method stub
  }

}
