package fleur.knime.nodes.statistics;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SummaryStatistics" Node. Extract basic summary statistics from
 * a set of FCS Files.
 *
 * @author Landysh
 */
public class SummaryStatisticsNodeFactory extends NodeFactory<SummaryStatisticsNodeModel> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeDialogPane createNodeDialogPane() {
    return new SummaryStatisticsNodeDialog();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SummaryStatisticsNodeModel createNodeModel() {
    return new SummaryStatisticsNodeModel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodeView<SummaryStatisticsNodeModel> createNodeView(final int viewIndex,
      final SummaryStatisticsNodeModel nodeModel) {
    return new SummaryStatisticsNodeView(nodeModel);
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
