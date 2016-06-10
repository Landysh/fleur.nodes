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

import io.landysh.inflor.java.core.FCSSummaryPanel;

public class ColumnStorePortSpec implements PortObjectSpec {
	
	public static final class Serializer extends PortObjectSpecSerializer<ColumnStorePortSpec> {
		@Override
		public ColumnStorePortSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
			return 	ColumnStorePortSpec.load(in);
		}

		@Override
		public void savePortObjectSpec(ColumnStorePortSpec spec, PortObjectSpecZipOutputStream out)
				throws IOException {
			spec.save(out);	
		}
	}
	
    private static final NodeLogger LOGGER = NodeLogger.getLogger(ColumnStorePortSpec.class);
	
	private final static String CFG_SPEC 		 = "spec";
	private final static String CFG_KEYS 		 = "keys";
	private final static String CFG_VALUES 		 = "values";
	private final static String CFG_COLUMN_NAMES = "vector names";
	private final static String CFG_RowCount	 = "row count";
	
	public Hashtable<String, String> 	keywords;
	public String[] 					columnNames;
	private int 						rowCount;
	
	public ColumnStorePortSpec(Hashtable<String, String> keys, String[] columns, int count) {
		keywords = keys;
		columnNames = columns;
		rowCount = count;
	}

	public static ColumnStorePortSpec load(PortObjectSpecZipInputStream in) {
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
        int newRowCount = 0;
        try {
        	keys = model.getStringArray(CFG_KEYS);
        	values = model.getStringArray(CFG_VALUES);
        	newVectorNames = model.getStringArray(CFG_COLUMN_NAMES);
        	newRowCount = model.getInt(CFG_RowCount);
        } catch (InvalidSettingsException ise) {
            LOGGER.error("Internal error: Could not load settings", ise);
        }
        Hashtable<String,String> newKeywords = new Hashtable<String,String>();
        for (int i=0;i<keys.length;i++){
        	newKeywords.put(keys[i], values[i]);
        }
        
        return new ColumnStorePortSpec(newKeywords, newVectorNames, newRowCount);
		
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
        modelOut.addStringArray(CFG_COLUMN_NAMES, columnNames);
        modelOut.addInt(CFG_RowCount, rowCount);
        try {
        	out.putNextEntry(new ZipEntry(CFG_SPEC));
        	modelOut.saveToXML(out);
        } catch (IOException ioe) {
            LOGGER.error("Internal error: Could not save settings", ioe);
        }
	}

	public ColumnStorePortSpec() {
		// no op, use with .load
	}

	@Override
	public JComponent[] getViews() {
        return new JComponent[]{new FCSSummaryPanel(keywords)};
	}

	public Hashtable<String, String> getKeywords() {
		return keywords;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public int getRowCount() {
		return rowCount;
	}

}
