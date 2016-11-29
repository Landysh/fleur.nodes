package io.landysh.inflor.main.knime.nodes.readFCS;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FCSFrameReader" Node. Reads a data file into an FCS Frame
 *
 * @author Aaron Hart
 */
public class ReadFCSFrameNodeFactory extends NodeFactory<ReadFCSFrameNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new ReadFCSNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ReadFCSFrameNodeModel createNodeModel() {
    return new ReadFCSFrameNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<ReadFCSFrameNodeModel> createNodeView(final int viewIndex,
      final ReadFCSFrameNodeModel nodeModel) {
    return new ReadFCSFrameNodeView(nodeModel);
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
