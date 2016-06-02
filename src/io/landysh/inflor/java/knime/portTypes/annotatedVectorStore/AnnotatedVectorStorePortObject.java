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

import io.landysh.inflor.java.core.AnnotatedVectorStore;

public class AnnotatedVectorStorePortObject extends FileStorePortObject {
	
	public static final class Serializer extends PortObjectSerializer <AnnotatedVectorStorePortObject> {
		
		@Override
        public void savePortObject(final AnnotatedVectorStorePortObject portObject, final PortObjectZipOutputStream out,
            final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			portObject.save(out);
		}

		@Override
		public AnnotatedVectorStorePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
				AnnotatedVectorStorePortObject avsPortObject = new AnnotatedVectorStorePortObject();
				avsPortObject.load(in, spec);
				return avsPortObject;
		}
	}
	
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(AnnotatedVectorStorePortObject.class);
	
 
	private AnnotatedVectorStoreSpec m_spec;
	private WeakReference<AnnotatedVectorStore> m_vectorStore;
	private String[] vectorNames;

	public void save(PortObjectZipOutputStream out) throws IOException {
        ModelContent content = new ModelContent("FOO");
        content.addStringArray("BAR", vectorNames);
        content.saveToXML(out);
		
	}
	
    private void load(final PortObjectZipInputStream in, final PortObjectSpec spec)
            throws IOException, CanceledExecutionException {
            m_spec = (AnnotatedVectorStoreSpec)spec;
            m_vectorStore = new WeakReference<AnnotatedVectorStore>(null);
            ModelContentRO contentRO = ModelContent.loadFromXML(in);
            try {
            	vectorNames = contentRO.getStringArray("BAR");
            } catch (InvalidSettingsException ise) {
                IOException ioe = new IOException("Unable to restore meta information: " + ise.getMessage());
                ioe.initCause(ise);
                throw ioe;
            }
        }

	public AnnotatedVectorStorePortObject(AnnotatedVectorStoreSpec spec, AnnotatedVectorStore vectorStore,
			FileStore fileStore) {
        super(Lists.newArrayList(fileStore));
       m_spec = spec;
       m_vectorStore =  new WeakReference<AnnotatedVectorStore>(vectorStore);
       vectorNames = vectorStore.vectorNames;
	}
	public AnnotatedVectorStorePortObject() {
		// to be used in conjunction only with .load().
	}

    public static AnnotatedVectorStorePortObject createPortObject(final AnnotatedVectorStoreSpec spec,
            final AnnotatedVectorStore vectorStore, final FileStore fileStore) {
            final AnnotatedVectorStorePortObject portObject = new AnnotatedVectorStorePortObject(spec, vectorStore, fileStore);
            try {	
                serialize(vectorStore, fileStore);
            } catch (IOException e) {
                throw new IllegalStateException("Something went wrong during serialization.", e);
            }
            return portObject;
        }
    private static void serialize(final AnnotatedVectorStore vectorStore, final FileStore fileStore) throws IOException {
        final File file = fileStore.getFile();
        try (FileOutputStream out = new FileOutputStream(file)) {
        	vectorStore.save(out);
        }
    }
    
    private AnnotatedVectorStore deserialize() throws IOException {
        final File file = getFileStore(0).getFile();
        AnnotatedVectorStore vectorStore;
        try (FileInputStream input = new FileInputStream(file)) {
        	try {
				vectorStore = AnnotatedVectorStore.load(input);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
        }
        return vectorStore;
    }
    
    public AnnotatedVectorStore getVectorStore(){
    	AnnotatedVectorStore vectorStore = m_vectorStore.get();
    	if (vectorStore== null){
    		try{
    			vectorStore = deserialize();
    		} catch (IOException e){
    			throw new IllegalStateException("Error in deserialization.", e);
    		}
    		m_vectorStore = new WeakReference<AnnotatedVectorStore>(vectorStore);
    	}
    	return vectorStore;
    }
    
	@Override
	public String getSummary() {
		Integer pCount = vectorNames.length;
		Integer rowCount = m_vectorStore.get().getColumnData().get(vectorNames[0]).length;
		String message = "vector set containing " + pCount + " parameters and " + rowCount + " rows ";
		return message;
	}

	@Override
	public PortObjectSpec getSpec() {
		return m_spec;
	}

	@Override
	public JComponent[] getViews() {
		return null;
	}

	public Hashtable<String, String> getHeader() {
		return m_spec.keywords;
	}

	public String[] getParameterList() {
		return vectorNames;
	}

	public Hashtable<String,Double[]> getData() {
		return m_vectorStore.get().getColumnData();
	}
}