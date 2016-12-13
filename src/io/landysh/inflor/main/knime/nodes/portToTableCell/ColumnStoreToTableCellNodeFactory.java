package io.landysh.inflor.main.knime.nodes.portToTableCell;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ColumnStoreToTableCell" Node. Converts a
 *
 * @author Landysh Co.
 */
public class ColumnStoreToTableCellNodeFactory
    extends NodeFactory<ColumnStoreToTableCellNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new ColumnStoreToTableCellNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ColumnStoreToTableCellNodeModel createNodeModel() {
    return new ColumnStoreToTableCellNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<ColumnStoreToTableCellNodeModel> createNodeView(final int viewIndex,
      final ColumnStoreToTableCellNodeModel nodeModel) {
    return new ColumnStoreToTableCellNodeView(nodeModel);
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
