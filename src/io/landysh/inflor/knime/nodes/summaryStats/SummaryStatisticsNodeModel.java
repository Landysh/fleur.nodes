package io.landysh.inflor.knime.nodes.summaryStats;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of SummaryStatistics. Extract basic summary statistics from a
 * set of FCS Files.
 *
 * @author Landysh
 */
public class SummaryStatisticsNodeModel extends NodeModel {

  SummaryStatsSettings m_settings;

  /**
   * Constructor for the node model.
   */
  protected SummaryStatisticsNodeModel() {

    super(1, 1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
      throws InvalidSettingsException {
    final DataTableSpec outSpec = createSpec(inSpecs[0]);
    return new DataTableSpec[] {outSpec};
  }

  private DataTableSpec createSpec(DataTableSpec inSpec) {
    final DataTableSpecCreator creator = new DataTableSpecCreator(inSpec);
    for (final String marker : m_settings.getResponseMarkersModel().getStringArrayValue()) {
      for (final String stat : m_settings.getSelectedStatsModel().getStringArrayValue()) {
        final DataColumnSpec spec =
            new DataColumnSpecCreator(stat + " " + marker, DoubleCell.TYPE).createSpec();
        creator.addColumns(spec);
      }
    }
    return creator.createSpec();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
      final ExecutionContext exec) throws Exception {

    // TODO: Return a BufferedDataTable for each output port
    return new BufferedDataTable[] {};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    m_settings.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    m_settings.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    m_settings.validate(settings);
  }
}
// EOF
