package io.landysh.inflor.main.knime.nodes.downsample;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.utils.BitSetUtils;
import io.landysh.inflor.main.core.utils.FCSUtilities;
import io.landysh.inflor.main.knime.portTypes.fcsFrame.FCSFramePortObject;
import io.landysh.inflor.main.knime.portTypes.fcsFrame.FCSFramePortSpec;

/**
 * This is the model implementation of Downsample.
 * 
 *
 * @author Landysh Incorportated
 */
public class DownsampleNodeModel extends NodeModel {

  // Downsample size
  static final String CFGKEY_Size = "size";
  static final int DEFAULT_Size = 5000;

  private final SettingsModelInteger m_Size = new SettingsModelInteger(CFGKEY_Size, DEFAULT_Size);

  /**
   * Constructor for the node model.
   */
  protected DownsampleNodeModel() {
    super(new PortType[] {PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)},
        new PortType[] {PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)});

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {
    final FCSFramePortSpec portSpec = (FCSFramePortSpec) inSpecs[0];

    final FCSFramePortSpec outSpec =
        new FCSFramePortSpec(portSpec.keywords, portSpec.columnNames, portSpec.getRowCount());
    return new FCSFramePortSpec[] {outSpec};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
      throws Exception {
    final FCSFramePortObject inPort = (FCSFramePortObject) inData[0];
    final FCSFramePortSpec inSpec = (FCSFramePortSpec) inPort.getSpec();
    final FCSFrame inColumnStore = inPort.getColumnStore();
    final int inSize = inColumnStore.getRowCount();
    final int downSize = m_Size.getIntValue();
    final int finalSize = downSize >= inSize ? downSize : inSize;
    FCSFrame outStore = new FCSFrame(inColumnStore.getKeywords(), finalSize);
    if (downSize >= inSize) {
      outStore.setData(inColumnStore.getData());
    } else {
      final BitSet mask = BitSetUtils.getShuffledMask(inSize, downSize);
      outStore = FCSUtilities.filterColumnStore(mask, inColumnStore);
    }

    final FCSFramePortSpec outSpec = getSpec(inSpec);
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    final FileStore filestore = fileStoreFactory.createFileStore("column.store");
    final FCSFramePortObject outPort =
        FCSFramePortObject.createPortObject(outSpec, outStore, filestore);

    return new FCSFramePortObject[] {outPort};
  }

  private FCSFramePortSpec getSpec(FCSFramePortSpec inSpec) {
    final FCSFramePortSpec outSpec =
        new FCSFramePortSpec(inSpec.keywords, inSpec.columnNames, inSpec.getRowCount());
    return outSpec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {

    m_Size.loadSettingsFrom(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    // TODO Code executed on reset.
    // Models build during execute are cleared here.
    // Also data handled in load/saveInternals will be erased here.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {

    m_Size.saveSettingsTo(settings);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    if (m_Size.getIntValue() >= 1) {
      m_Size.validateSettings(settings);
    } else {
      throw new InvalidSettingsException("Downsample size must be greater than 1");
    }
  }

}
