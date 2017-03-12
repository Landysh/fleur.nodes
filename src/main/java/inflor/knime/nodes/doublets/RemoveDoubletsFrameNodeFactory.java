package inflor.knime.nodes.doublets;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FindSingletsFrame" Node.
 * 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeFactory extends NodeFactory<RemoveDoubletsFrameNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new RemoveDoubletsFrameNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RemoveDoubletsFrameNodeModel createNodeModel() {
    return new RemoveDoubletsFrameNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<RemoveDoubletsFrameNodeModel> createNodeView(final int viewIndex,
      final RemoveDoubletsFrameNodeModel nodeModel) {
    return new RemoveDoubletsFrameNodeView(nodeModel);
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
