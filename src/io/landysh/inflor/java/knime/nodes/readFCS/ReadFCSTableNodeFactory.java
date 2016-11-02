package io.landysh.inflor.java.knime.nodes.readFCS;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "FCSReader" Node. It will do stuff
 *
 * @author Landysh
 */
public class ReadFCSTableNodeFactory extends NodeFactory<ReadFCSTableNodeModel> {

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
  public ReadFCSTableNodeModel createNodeModel() {
    return new ReadFCSTableNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<ReadFCSTableNodeModel> createNodeView(final int viewIndex,
      final ReadFCSTableNodeModel nodeModel) {
    return new ReadFCSTableNodeView(nodeModel);
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
