package io.landysh.inflor.java.knime.portTypes.fcs;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

public class FCSFramePortObject extends AbstractSimplePortObject implements PortObject {
	
	public Hashtable<String, String> 	header;
	public String[] 					parameterList;
	public Hashtable<String, double[]> 					parameterData;

	public FCSFramePortObject(FCSFrameSpec spec, Hashtable<String, double[]> columns) {
		header = spec.header;
		parameterList = spec.parameterList;
		parameterData = columns;
	}
	@Override
	public String getSummary() {
		Integer pCount = parameterList.length;
		Integer rowCount = parameterData.get(parameterList[0]).length;
		String message = "FCS array containing " + pCount + " parameters and " + rowCount + " rows ";
		return message;
	}

	@Override
	public PortObjectSpec getSpec() {
		PortObjectSpec p = new FCSFrameSpec(header,parameterList);
		return p;
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void save(ModelContentWO model, ExecutionMonitor exec) throws CanceledExecutionException {
		// Save header
		Enumeration<String> keys = header.keys();
		int i=0;
		ArrayList<String> keywords = new ArrayList<String>();
		ArrayList<String> keywordValues = new ArrayList<String>();
		while (header.keys().hasMoreElements()) {
			String key = keys.nextElement();
			keywords.add(key);
			keywordValues.add(header.get(key));
			// the cells of the current row, the types of the cells must match 
			// the column spec (see above)
			i++;
			if (key.equals("0") && header.get(key).equals("0"))
				header.remove(key);
		}
		model.addStringArray("keywords", (String[]) keywords.toArray());
		model.addStringArray("keywordValues", (String[]) keywordValues.toArray());
		
		// Save parameterList
		model.addStringArray("parameters", parameterList);
		
		// Save data
		for (i=0; i<parameterList.length; i++){
			model.addDoubleArray(parameterList[i], parameterData.get(parameterList[i]));
		}
		
	}

	@Override
	protected void load(ModelContentRO model, PortObjectSpec spec, ExecutionMonitor exec)
			throws InvalidSettingsException, CanceledExecutionException {

		// Load header
		String[] keywords = model.getStringArray("keywords");
		String[] keywordValues = model.getStringArray("keywordValues");
		Hashtable<String, String> newHeader = new Hashtable<String, String>();
		for (int i=0; i<keywords.length; i++){
			newHeader.put(keywords[i], keywordValues[i]);
		}
		header = newHeader;
		//Load parameterList
		parameterList = model.getStringArray("parameters");
		
		// Load data
		Hashtable<String, double[]> newData = null;
		for (int i=0; i<parameterList.length; i++){
			model.getDoubleArray(parameterList[i], parameterData.get(parameterList[i]));
		}
		parameterData = newData;
	}

	public Hashtable<String, String> getHeader() {
		return header;
	}

	public String[] getParameterList() {
		return parameterList;
	}

	public Hashtable<String, double[]> getData() {
		return parameterData;
	}
	
}