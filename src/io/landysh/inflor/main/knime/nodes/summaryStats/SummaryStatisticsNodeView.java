package io.landysh.inflor.main.knime.nodes.summaryStats;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SummaryStatistics" Node. Extract basic summary statistics from a
 * set of FCS Files.
 *
 * @author Landysh
 */
public class SummaryStatisticsNodeView extends NodeView<SummaryStatisticsNodeModel> {

  /**
   * Creates a new view.
   * 
   * @param nodeModel The model (class: {@link SummaryStatisticsNodeModel})
   */
  protected SummaryStatisticsNodeView(final SummaryStatisticsNodeModel nodeModel) {
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
