package io.landysh.inflor.java.knime.nodes.removeDoublets;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.knime.portTypes.fcsFrame.FCSFramePortObject;
import io.landysh.inflor.java.knime.portTypes.fcsFrame.FCSFramePortSpec;

/**
 * This is the model implementation of FindSingletsFrame.
 * 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeModel extends NodeModel {

  // Area parameter
  static final String CFGKEY_AreaColumn = "Area Column";
  static final String DEFAULT_AreaColumn = null;
  // Height parameter
  static final String CFGKEY_HeightColumn = "Height Column";

  static final String DEFAULT_HeightColumn = null;
  private final SettingsModelString m_AreaColumn =
      new SettingsModelString(CFGKEY_AreaColumn, DEFAULT_AreaColumn);
  private final SettingsModelString m_HeightColumn =
      new SettingsModelString(CFGKEY_HeightColumn, DEFAULT_HeightColumn);

  /**
   * Constructor for the node model.
   */
  protected RemoveDoubletsFrameNodeModel() {

    super(new PortType[] {PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)},
        new PortType[] {PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class)});

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {
    final FCSFramePortSpec inSpec = (FCSFramePortSpec) inSpecs[0];

    return new FCSFramePortSpec[] {getSpec(inSpec)};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec)
      throws Exception {

    // get the data
    final FCSFramePortObject inPort = (FCSFramePortObject) inData[0];
    final FCSFramePortSpec inSpec = (FCSFramePortSpec) inPort.getSpec();
    final FCSFrame inColumnStore = inPort.getColumnStore();

    // Do the modeling
    final SingletsModel model = new SingletsModel(inSpec.columnNames);
    final double[] area = inColumnStore.getFCSDimensionByShortName(m_AreaColumn.getStringValue()).getData();
    final double[] height = inColumnStore.getFCSDimensionByShortName(m_HeightColumn.getStringValue()).getData();
    final double[] ratio = model.buildModel(area, height);
    final BitSet mask = model.scoreModel(ratio);

    // Create the output
    final FCSFrame outStore = FCSUtilities.filterColumnStore(mask, inColumnStore);
    final FCSFramePortSpec outSpec =
        new FCSFramePortSpec(inSpec.keywords, inSpec.columnNames, outStore.getRowCount());
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    final FileStore filestore = fileStoreFactory.createFileStore("column.store");
    final FCSFramePortObject outPort =
        FCSFramePortObject.createPortObject(outSpec, outStore, filestore);
    return new PortObject[] {outPort};
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
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {
    m_AreaColumn.setStringValue(settings.getString(CFGKEY_AreaColumn));
    m_HeightColumn.setStringValue(settings.getString(CFGKEY_HeightColumn));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {
    // TODO: generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    settings.addString(CFGKEY_AreaColumn, m_AreaColumn.getStringValue());
    settings.addString(CFGKEY_HeightColumn, m_HeightColumn.getStringValue());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    // TODO: generated method stub
  }

}
