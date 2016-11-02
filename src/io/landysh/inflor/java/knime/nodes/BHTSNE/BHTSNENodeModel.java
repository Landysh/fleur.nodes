package io.landysh.inflor.java.knime.nodes.BHTSNE;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.jujutsu.tsne.FastTSne;

import io.landysh.inflor.java.knime.portTypes.fcsFrame.FCSFramePortObject;

/**
 * This is the model implementation of BHTSNE.
 * 
 *
 * @author Landysh Co.
 */
public class BHTSNENodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(BHTSNENodeModel.class);

  BHTSNESettingsModel m_settings = new BHTSNESettingsModel();

  /**
   * Constructor for the node model.
   */
  protected BHTSNENodeModel() {

    super(new PortType[] {FCSFramePortObject.TYPE}, new PortType[] {BufferedDataTable.TYPE});
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {

    return new DataTableSpec[] {createSpec(inSpecs[0])};
  }

  private DataTableSpec createSpec(PortObjectSpec inSpec) {
    final String[] features = m_settings.getFeatures();

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
      throws CanceledExecutionException {

    // get the data and write it to the container
    final FCSFramePortObject port = ((FCSFramePortObject) inData[0]);
    final int rowCount = port.getColumnStore().getRowCount();

    // Get the settings we'll need
    final String[] features = m_settings.getFeatures();
    final int maxIters = m_settings.getIterations();
    final int perplexity = m_settings.getPerplexity();
    final int pcaDims = m_settings.getPCADims();
    final int outDims = m_settings.getFinalDimensions();

    // Initialize the data
    final double[][] X = new double[features.length][rowCount];

    for (int i = 0; i < features.length; i++) {
      X[i] = port.getColumnStore().getFCSDimension(features[i]).getData();
    }

    final FastTSne sne = new FastTSne();

    final double[][] Y = sne.tsne(X, outDims, pcaDims, perplexity, maxIters);

    // create the output container
    final DataTableSpec spec = createSpec(inData[0].getSpec());
    final BufferedDataContainer container = exec.createDataContainer(spec);

    for (int i = 0; i < rowCount; i++) {
      final DataCell[] dataCells = new DataCell[X.length + Y.length];
      for (int j = 0; j < X.length; j++) {
        dataCells[j] = new DoubleCell(X[j][i]);
      }
      for (int k = 0; k < Y.length; k++) {
        dataCells[X.length + k] = new DoubleCell(X[k][i]);
      }
      final DataRow dataRow = new DefaultRow("Row " + i, dataCells);
      container.addRowToTable(dataRow);
    }

    // cleanup and create the table
    container.close();
    final BufferedDataTable table = container.getTable();
    return new BufferedDataTable[] {table};
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
  protected void reset() {

  }

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
