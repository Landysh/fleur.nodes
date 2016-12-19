package io.landysh.inflor.main.knime.ports.fcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import com.google.common.collect.Lists;

import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameFileStoreDataCell;
import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.knime.dataTypes.FCSFrameCell.FCSFrameContent;

public class FCSFramePortObject extends FileStorePortObject {

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

  public static final PortType TYPE =
      PortTypeRegistry.getInstance().getPortType(FCSFramePortObject.class);

  private static final String COLUMNS_NAME = "column_names";
  private static final String MODEL_NAME = "column_store_model";

  public static FCSFramePortObject createPortObject(final FCSFramePortSpec spec,
      final FCSFrame columnStore, final FileStore fileStore) {
    final FCSFramePortObject portObject =
        new FCSFramePortObject(spec, columnStore, fileStore);
    try {
      serialize(columnStore, fileStore);
    } catch (final IOException e) {
      throw new IllegalStateException("Something went wrong during serialization.", e);
    }
    return portObject;
  }

  private static void serialize(final FCSFrame vectorStore, final FileStore fileStore)
      throws IOException {
    final File file = fileStore.getFile();
    try (FileOutputStream out = new FileOutputStream(file)) {
      vectorStore.save(out);
    }
  }

  private FCSFramePortSpec m_spec;

  private WeakReference<FCSFrameContent> m_columnStore;

  private List<String> m_columnNames;

  public FCSFramePortObject() {
    // to be used in conjunction only with .load().
  }

  public FCSFramePortObject(FCSFramePortSpec spec, FCSFrame vectorStore,
      FileStore fileStore) {
    super(Lists.newArrayList(fileStore));
    m_spec = spec;
    final FCSFrameContent content = new FCSFrameContent(vectorStore);
    m_columnStore = new WeakReference<FCSFrameContent>(content);
    m_columnNames = vectorStore.getColumnNames();
  }

  private FCSFrame deserialize() throws IOException {
    final File file = getFileStore(0).getFile();
    FCSFrame vectorStore;
    try {
      final FileInputStream input = new FileInputStream(file);
      vectorStore = FCSFrame.load(input);
    } catch (final Exception e) {
      e.printStackTrace();
      throw new IOException();
    }
    return vectorStore;
  }

  public FCSFrame getColumnStore() {
    final FCSFrameContent content = m_columnStore.get();
    FCSFrame cs = null;
    if (content == null) {
      try {
        cs = deserialize();
      } catch (final IOException e) {
        throw new IllegalStateException("Error in deserialization.", e);
      }
      final FCSFrameContent newContent = new FCSFrameContent(cs);
      m_columnStore = new WeakReference<FCSFrameContent>(newContent);
    }
    return cs;
  }

  public Map<String, String> getHeader() {
    return m_spec.keywords;
  }

  public List<String> getParameterList() {
    return m_columnNames;
  }

  @Override
  public PortObjectSpec getSpec() {
    return m_spec;
  }

  @Override
  public String getSummary() {
    final Integer pCount = m_columnNames.size();
    final Integer rowCount = m_columnStore.get().getColumnStore().getRowCount();
    final String message =
        "vector set containing " + pCount + " parameters and " + rowCount + " rows ";
    return message;
  }

  private void load(final PortObjectZipInputStream in, final PortObjectSpec spec)
      throws IOException, CanceledExecutionException {
    m_spec = (FCSFramePortSpec) spec;
    m_columnStore = new WeakReference<FCSFrameContent>(null);
    final ModelContentRO contentRO = ModelContent.loadFromXML(in);
    try {
      m_columnNames = Arrays.asList(contentRO.getStringArray(COLUMNS_NAME));
    } catch (final InvalidSettingsException ise) {
      final IOException ioe =
          new IOException("Unable to restore meta information: " + ise.getMessage());
      ioe.initCause(ise);
      throw ioe;
    }
  }

  public void save(PortObjectZipOutputStream out) throws IOException {
    final ModelContent content = new ModelContent(MODEL_NAME);
    content.addStringArray(COLUMNS_NAME, m_columnNames.toArray(new String[m_columnNames.size()]));
    content.saveToXML(out);

  }

  public FCSFrameFileStoreDataCell toTableCell(FileStore fs) {
    getColumnStore();
    return m_columnStore.get().toColumnStoreCell(fs);
  }

  @Override
  public JComponent[] getViews() {
    // TODO Auto-generated method stub
    return null;
  }
}
