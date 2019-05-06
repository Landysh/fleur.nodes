package fleur.knime.ports.fcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import com.google.common.collect.Lists;

import fleur.core.data.FCSFrame;
import fleur.knime.core.NodeUtilities;
import fleur.knime.data.type.cell.fcs.FCSFrameContent;
import fleur.knime.data.type.cell.fcs.FCSFrameFileStoreDataCell;
import fleur.knime.data.type.cell.fcs.FCSFrameMetaData;

public class FCSFramePortObject extends FileStorePortObject {

  private static final NodeLogger logger = NodeLogger.getLogger(FCSFramePortObject.class);

  public static final PortType TYPE =
      PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class);

  private static final String COLUMNS_NAME = "column_names";
  private static final String MODEL_NAME = "column_store_model";
  
  private FCSFramePortSpec mSpec;
  private WeakReference<FCSFrameContent> mContent;
  private List<String> mDimensionNames;

  public FCSFramePortObject() {
    // to be used in conjunction only with .load().
  }

  public FCSFramePortObject(FCSFramePortSpec spec, FCSFrameMetaData metaData, FileStore fileStore) {
    super(Lists.newArrayList(fileStore));
    mSpec = spec;
    final FCSFrameContent content = new FCSFrameContent(fileStore, metaData);
    mContent = new WeakReference<>(content);
    mDimensionNames = Arrays.asList(metaData.getDimensionNames());
  }
  
  public static final class Serializer extends PortObjectSerializer<FCSFramePortObject> {

    @Override
    public FCSFramePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
        ExecutionMonitor exec) throws IOException, CanceledExecutionException {
      final FCSFramePortObject avsPortObject = new FCSFramePortObject();
      avsPortObject.load(in, spec);
      return avsPortObject;
    }

    @Override
    public void savePortObject(final FCSFramePortObject portObject,
        final PortObjectZipOutputStream out, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
      portObject.save(out);
    }
  }

  public static FCSFramePortObject createPortObject(final FCSFramePortSpec spec,
      final FCSFrameMetaData metaData, final FileStore fileStore) {
    return new FCSFramePortObject(spec, metaData, fileStore);
  }

  private FCSFrame deserialize() throws IOException {
    final File file = getFileStore(0).getFile();
    FCSFrame df;
    try {
      final FileInputStream input = new FileInputStream(file);
      df = FCSFrame.load(input);
    } catch (final Exception e) {
      logger.error("Unable to deserialize port object", e);
      throw new IOException();
    }
    return df;
  }

  public FCSFrame getColumnStore(ExecutionContext exec) throws IOException {
    final FCSFrameContent content = mContent.get();
    FCSFrame df;
    if (content == null) {
        df = deserialize();
        FileStoreFactory factory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
        
        String fsName = NodeUtilities.getFileStoreName(df);
        FileStore fs = factory.createFileStore(fsName);
        int size = NodeUtilities.writeFrameToFilestore(df, fs);
        FCSFrameMetaData metaData = new FCSFrameMetaData(df, size);
        final FCSFrameContent newContent = new FCSFrameContent(fs, metaData);
        mContent = new WeakReference<>(newContent);
        return df;
    } else {
      return deserialize();
    }
  }

  public Map<String, String> getHeader() {
    return mSpec.getKeywords();
  }

  public List<String> getParameterList() {
    return mDimensionNames;
  }

  @Override
  public PortObjectSpec getSpec() {
    return mSpec;
  }

  @Override
  public String getSummary() {
    final Integer pCount = mDimensionNames.size();
    final Integer rowCount = mContent.get().getColumnStore().getRowCount();
    return "vector set containing " + pCount + " parameters and " + rowCount + " rows ";
  }

  private void load(final PortObjectZipInputStream in, final PortObjectSpec spec)
      throws IOException, CanceledExecutionException {
    mSpec = (FCSFramePortSpec) spec;
    mContent = new WeakReference<>(null);
    final ModelContentRO contentRO = ModelContent.loadFromXML(in);
    try {
      mDimensionNames = Arrays.asList(contentRO.getStringArray(COLUMNS_NAME));
    } catch (final InvalidSettingsException ise) {
      final IOException ioe =
          new IOException("Unable to restore meta information: " + ise.getMessage());
      ioe.initCause(ise);
      throw ioe;
    }
  }

  public void save(PortObjectZipOutputStream out) throws IOException {
    final ModelContent content = new ModelContent(MODEL_NAME);
    content.addStringArray(COLUMNS_NAME, mDimensionNames.toArray(new String[mDimensionNames.size()]));
    content.saveToXML(out);

  }

  public FCSFrameFileStoreDataCell toTableCell(FileStore fs, int size) {
    return mContent.get().toColumnStoreCell(fs, size);
  }

  @Override
  public JComponent[] getViews() {
    return new JComponent[]{};
  }
}
