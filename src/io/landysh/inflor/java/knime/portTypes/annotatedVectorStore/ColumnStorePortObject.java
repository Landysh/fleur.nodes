package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
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

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreContent;

public class ColumnStorePortObject extends FileStorePortObject {

	public static final class Serializer extends PortObjectSerializer<ColumnStorePortObject> {

		@Override
		public ColumnStorePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			final ColumnStorePortObject avsPortObject = new ColumnStorePortObject();
			avsPortObject.load(in, spec);
			return avsPortObject;
		}

		@Override
		public void savePortObject(final ColumnStorePortObject portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			portObject.save(out);
		}
	}

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class);

	private static final String COLUMNS_NAME = "column_names";
	private static final String MODEL_NAME = "column_store_model";

	public static ColumnStorePortObject createPortObject(final ColumnStorePortSpec spec, final FCSFrame columnStore,
			final FileStore fileStore) {
		final ColumnStorePortObject portObject = new ColumnStorePortObject(spec, columnStore, fileStore);
		try {
			serialize(columnStore, fileStore);
		} catch (final IOException e) {
			throw new IllegalStateException("Something went wrong during serialization.", e);
		}
		return portObject;
	}

	private static void serialize(final FCSFrame vectorStore, final FileStore fileStore) throws IOException {
		final File file = fileStore.getFile();
		try (FileOutputStream out = new FileOutputStream(file)) {
			vectorStore.save(out);
		}
	}

	private ColumnStorePortSpec m_spec;

	private WeakReference<ColumnStoreContent> m_columnStore;

	private String[] m_columnNames;

	public ColumnStorePortObject() {
		// to be used in conjunction only with .load().
	}

	public ColumnStorePortObject(ColumnStorePortSpec spec, FCSFrame vectorStore, FileStore fileStore) {
		super(Lists.newArrayList(fileStore));
		m_spec = spec;
		final ColumnStoreContent content = new ColumnStoreContent(vectorStore);
		m_columnStore = new WeakReference<ColumnStoreContent>(content);
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
		final ColumnStoreContent content = m_columnStore.get();
		FCSFrame cs = null;
		if (content == null) {
			try {
				cs = deserialize();
			} catch (final IOException e) {
				throw new IllegalStateException("Error in deserialization.", e);
			}
			final ColumnStoreContent newContent = new ColumnStoreContent(cs);
			m_columnStore = new WeakReference<ColumnStoreContent>(newContent);
		}
		return cs;
	}

	public HashMap<String, String> getHeader() {
		return m_spec.keywords;
	}

	public String[] getParameterList() {
		return m_columnNames;
	}

	@Override
	public PortObjectSpec getSpec() {
		return m_spec;
	}

	@Override
	public String getSummary() {
		final Integer pCount = m_columnNames.length;
		final Integer rowCount = m_columnStore.get().getColumnStore().getRowCount();
		final String message = "vector set containing " + pCount + " parameters and " + rowCount + " rows ";
		return message;
	}

	private void load(final PortObjectZipInputStream in, final PortObjectSpec spec)
			throws IOException, CanceledExecutionException {
		m_spec = (ColumnStorePortSpec) spec;
		m_columnStore = new WeakReference<ColumnStoreContent>(null);
		final ModelContentRO contentRO = ModelContent.loadFromXML(in);
		try {
			m_columnNames = contentRO.getStringArray(COLUMNS_NAME);
		} catch (final InvalidSettingsException ise) {
			final IOException ioe = new IOException("Unable to restore meta information: " + ise.getMessage());
			ioe.initCause(ise);
			throw ioe;
		}
	}

	public void save(PortObjectZipOutputStream out) throws IOException {
		final ModelContent content = new ModelContent(MODEL_NAME);
		content.addStringArray(COLUMNS_NAME, m_columnNames);
		content.saveToXML(out);

	}

	public ColumnStoreCell toTableCell(FileStore fs) {
		getColumnStore();
		return m_columnStore.get().toColumnStoreCell(fs);
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}
}