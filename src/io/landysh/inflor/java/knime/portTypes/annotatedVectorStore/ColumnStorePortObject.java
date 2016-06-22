package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

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

import io.landysh.inflor.java.core.ColumnStore;
import io.landysh.inflor.java.core.views.ColumnStoreViewFactory;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreContent;

public class ColumnStorePortObject extends FileStorePortObject {
	
	public static final class Serializer extends PortObjectSerializer <ColumnStorePortObject> {
		
		@Override
        public void savePortObject(final ColumnStorePortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			portObject.save(out);
		}

		@Override
		public ColumnStorePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
				ColumnStorePortObject avsPortObject = new ColumnStorePortObject();
				avsPortObject.load(in, spec);
				return avsPortObject;
		}
	}
	
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class);


	private static final String COLUMNS_NAME = "column_names";
	private static final String MODEL_NAME = "column_store_model";

 
	private ColumnStorePortSpec m_spec;
	private WeakReference<ColumnStoreContent> m_columnStore;
	private String[] m_columnNames;



	public void save(PortObjectZipOutputStream out) throws IOException {
        ModelContent content = new ModelContent(MODEL_NAME);
        content.addStringArray(COLUMNS_NAME, m_columnNames);
        content.saveToXML(out);
		
	}
	
    private void load(final PortObjectZipInputStream in, final PortObjectSpec spec)
            throws IOException, CanceledExecutionException {
            m_spec = (ColumnStorePortSpec)spec;
            m_columnStore = new WeakReference<ColumnStoreContent>(null);
            ModelContentRO contentRO = ModelContent.loadFromXML(in);
            try {
            	m_columnNames = contentRO.getStringArray(COLUMNS_NAME);
            } catch (InvalidSettingsException ise) {
                IOException ioe = new IOException("Unable to restore meta information: " + ise.getMessage());
                ioe.initCause(ise);
                throw ioe;
            }
        }

	public ColumnStorePortObject(ColumnStorePortSpec spec, ColumnStore vectorStore,
			FileStore fileStore) {
       super(Lists.newArrayList(fileStore));
       m_spec = spec;
       ColumnStoreContent content = new ColumnStoreContent(vectorStore);
       m_columnStore =  new WeakReference<ColumnStoreContent>(content);
       m_columnNames = vectorStore.getColumnNames();
	}
	public ColumnStorePortObject() {
		// to be used in conjunction only with .load().
	}

    public static ColumnStorePortObject createPortObject(final ColumnStorePortSpec spec,
            final ColumnStore columnStore, final FileStore fileStore) {
            final ColumnStorePortObject portObject = new ColumnStorePortObject(spec, columnStore, fileStore);
            try {	
                serialize(columnStore, fileStore);
            } catch (IOException e) {
                throw new IllegalStateException("Something went wrong during serialization.", e);
            }
            return portObject;
        }
    private static void serialize(final ColumnStore vectorStore, final FileStore fileStore) throws IOException {
        final File file = fileStore.getFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
        	vectorStore.save(out);
        }
    }
    
    private ColumnStore deserialize() throws IOException {
        final File file = getFileStore(0).getFile();
        ColumnStore vectorStore;
        try {
        	FileInputStream input = new FileInputStream(file);
        	vectorStore = ColumnStore.load(input);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException();
		}
        return vectorStore;
    }
    
    public ColumnStore getColumnStore(){
    	ColumnStoreContent content = m_columnStore.get();
    	ColumnStore cs = null;
    	if (content == null){
    		try{
    			cs = deserialize();
    		} catch (IOException e){
    			throw new IllegalStateException("Error in deserialization.", e);
    		}
    		ColumnStoreContent newContent = new ColumnStoreContent(cs);
    		m_columnStore = new WeakReference<ColumnStoreContent>(newContent);
    	}
    	return cs;
    }
    
	@Override
	public String getSummary() {
		Integer pCount = m_columnNames.length;
		Integer rowCount = m_columnStore.get().getColumnStore().getRowCount();
		String message = "vector set containing " + pCount + " parameters and " + rowCount + " rows ";
		return message;
	}

	@Override
	public PortObjectSpec getSpec() {
		return m_spec;
	}

	@Override
	public JComponent[] getViews() {
		ColumnStore columnStore = getColumnStore();
		JComponent lineageView  = ColumnStoreViewFactory.createLineageView(columnStore);
		JComponent[] components = new JComponent[] {lineageView};
		return components;
	}

	public Hashtable<String, String> getHeader() {
		return m_spec.keywords;
	}

	public String[] getParameterList() {
		return m_columnNames;
	}

	public ColumnStoreCell toTableCell(FileStore fs) {
		getColumnStore();
		return m_columnStore.get().toColumnStoreCell(fs);
	}
}