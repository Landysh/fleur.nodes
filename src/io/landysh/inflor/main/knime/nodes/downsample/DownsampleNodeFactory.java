package io.landysh.inflor.main.knime.nodes.downsample;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Downsample" Node.
 * 
 *
 * @author Landysh Incorportated
 */
public class DownsampleNodeFactory extends NodeFactory<DownsampleNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new DownsampleNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DownsampleNodeModel createNodeModel() {
    return new DownsampleNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<DownsampleNodeModel> createNodeView(final int viewIndex,
      final DownsampleNodeModel nodeModel) {
    return new DownsampleNodeView(nodeModel);
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
