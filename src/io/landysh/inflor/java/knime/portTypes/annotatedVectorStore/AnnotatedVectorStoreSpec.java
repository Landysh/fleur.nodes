package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;

import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

import io.landysh.inflor.java.core.FCSSummaryView;

public class AnnotatedVectorStoreSpec implements PortObjectSpec {
	
	public static final class Serializer extends PortObjectSpecSerializer<AnnotatedVectorStoreSpec> {
		@Override
		public AnnotatedVectorStoreSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			return 	AnnotatedVectorStoreSpec.load(in);
		}

		@Override
		public void savePortObjectSpec(AnnotatedVectorStoreSpec spec, PortObjectSpecZipOutputStream out)
				throws IOException {
			spec.save(out);	
		}
	}
	
    private static final NodeLogger LOGGER = NodeLogger.getLogger(AnnotatedVectorStoreSpec.class);
	
	private final static String CFG_SPEC 		 = "spec";
	private final static String CFG_KEYS 		 = "keys";
	private final static String CFG_VALUES 		 = "values";
	private final static String CFG_VECTOR_NAMES = "vector names";
	
	public Hashtable<String, String> 	keywords;
	public String[] 					vectorNames;
	
	public AnnotatedVectorStoreSpec(Hashtable<String, String> inKeys, String[] plist) {
		keywords = inKeys;
		vectorNames = plist;
	}

	public static AnnotatedVectorStoreSpec load(PortObjectSpecZipInputStream in) {
		ModelContentRO model = null;
        try {
            ZipEntry zentry = in.getNextEntry();
            assert zentry.getName().equals(CFG_SPEC);
            model = ModelContent.loadFromXML(in);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not load settings", ioe);
        }
        String[] keys = null;
        String[] values = null;
        String[] newVectorNames = null;
        try {
        	keys = model.getStringArray(CFG_KEYS);
        	values = model.getStringArray(CFG_VALUES);
        	newVectorNames = model.getStringArray(CFG_VECTOR_NAMES);
        } catch (InvalidSettingsException ise) {
            LOGGER.error("Internal error: Could not load settings", ise);
        }
        Hashtable<String,String> newKeywords = new Hashtable<String,String>();
        for (int i=0;i<keys.length;i++){
        	newKeywords.put(keys[i], values[i]);
        }
        
        return new AnnotatedVectorStoreSpec(newKeywords, newVectorNames);
		
	}

	public void save(PortObjectSpecZipOutputStream out) {
		//Build the keyword map.
		String[] keys = new String[keywords.keySet().size()];
		String[] values = new String[keywords.keySet().size()];
		int i=0;
		for (String key : keywords.keySet() ){
			keys[i] = key;
			values[i] = keywords.get(key);
			i++;
		}
       //Create model and add values.
		ModelContent modelOut = new ModelContent(CFG_SPEC);
        modelOut.addStringArray(CFG_KEYS, keys);
        modelOut.addStringArray(CFG_VALUES, values);
        modelOut.addStringArray(CFG_VECTOR_NAMES, vectorNames);
        try {
        	out.putNextEntry(new ZipEntry(CFG_SPEC));
        	modelOut.saveToXML(out);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not save settings", ioe);
        }
	}

	public AnnotatedVectorStoreSpec() {
		// no op, use with .load
	}

	@Override
	public JComponent[] getViews() {
        return new JComponent[]{new FCSSummaryView(keywords)};
	}

	public Hashtable<String, String> getHeader() {
		return keywords;
	}

	public String[] getParameterList() {
		return vectorNames;
	}

}
