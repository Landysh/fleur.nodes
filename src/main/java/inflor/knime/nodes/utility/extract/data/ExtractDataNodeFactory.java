package main.java.inflor.knime.nodes.utility.extract.data;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ExtractTrainingSet" Node. Extracts data from an FCS frame
 * column to a standard KNIME Table. Data may be transformed and gating information included for
 * downstream ML applications.
 *
 * @author Aaron Hart
 */
public class ExtractDataNodeFactory extends NodeFactory<ExtractDataNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ExtractDataNodeModel createNodeModel() {
    return new ExtractDataNodeModel();
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
  public NodeView<ExtractDataNodeModel> createNodeView(final int viewIndex,
      final ExtractDataNodeModel nodeModel) {
    return new ExtractDataNodeView(nodeModel);
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
    return new ExtractDataNodeDialog();
  }

}

