package main.java.inflor.knime.nodes.utility.extractsubset;

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
public class ExtractTrainingSetNodeFactory extends NodeFactory<ExtractTrainingSetNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ExtractTrainingSetNodeModel createNodeModel() {
    return new ExtractTrainingSetNodeModel();
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
  public NodeView<ExtractTrainingSetNodeModel> createNodeView(final int viewIndex,
      final ExtractTrainingSetNodeModel nodeModel) {
    return new ExtractTrainingSetNodeView(nodeModel);
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
    return new ExtractTrainingSetNodeDialog();
  }

}

