package inflor.knime.nodes.doublets;

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

import inflor.core.data.FCSFrame;
import inflor.core.singlets.SingletsModel;
import inflor.core.utils.FCSUtilities;
import inflor.knime.core.NodeUtilities;
import inflor.knime.data.type.cell.fcs.FCSFrameMetaData;
import inflor.knime.ports.fcs.FCSFramePortObject;
import inflor.knime.ports.fcs.FCSFramePortSpec;

/**
 * This is the model implementation of FindSingletsFrame.
 * 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeModel extends NodeModel {

  // Area parameter
  static final String KEY_AREA_COLUMN = "Area Column";
  static final String DEFAULT_AREA_COLUMN = null;
  // Height parameter
  static final String KEY_HEIGHT_COLUMN = "Height Column";
  static final String DEFAULT_HEIGHT_COLUMN = null;
  private final SettingsModelString mAreaColumn =
      new SettingsModelString(KEY_AREA_COLUMN, DEFAULT_AREA_COLUMN);
  private final SettingsModelString mHeightColumn =
      new SettingsModelString(KEY_HEIGHT_COLUMN, DEFAULT_HEIGHT_COLUMN);

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
    final FCSFrame inFrame = inPort.getColumnStore(exec);

    // Do the modeling
    final SingletsModel model = new SingletsModel(inSpec.getColumnNames());
    final double[] area = inFrame.getDimension(mAreaColumn.getStringValue()).getData();
    final double[] height = inFrame.getDimension(mHeightColumn.getStringValue()).getData();
    final double[] ratio = model.buildModel(area, height);
    final BitSet mask = model.scoreModel(ratio);

    // Create the output
    final FCSFrame outFrame = FCSUtilities.filterFrame(mask, inFrame);
    final FCSFramePortSpec outSpec =
        new FCSFramePortSpec(inSpec.getKeywords(), inSpec.getColumnNames(), outFrame.getRowCount());
    final FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
    String fsName = NodeUtilities.getFileStoreName(outFrame);
    final FileStore filestore = fileStoreFactory.createFileStore(fsName);
    int size = NodeUtilities.writeFrameToFilestore(outFrame, filestore);
    FCSFrameMetaData metaData = new FCSFrameMetaData(outFrame, size);

    final FCSFramePortObject outPort =
        FCSFramePortObject.createPortObject(outSpec, metaData, filestore);
    return new PortObject[] {outPort};
  }

  private FCSFramePortSpec getSpec(FCSFramePortSpec inSpec) {
    return new FCSFramePortSpec(inSpec.getKeywords(), inSpec.getColumnNames(),
        inSpec.getRowCount());
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
    mAreaColumn.setStringValue(settings.getString(KEY_AREA_COLUMN));
    mHeightColumn.setStringValue(settings.getString(KEY_HEIGHT_COLUMN));

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
    settings.addString(KEY_AREA_COLUMN, mAreaColumn.getStringValue());
    settings.addString(KEY_HEIGHT_COLUMN, mHeightColumn.getStringValue());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
    // TODO: generated method stub
  }

}
