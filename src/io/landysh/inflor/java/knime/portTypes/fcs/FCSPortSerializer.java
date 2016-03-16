package io.landysh.inflor.java.knime.portTypes.fcs;

import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.ArrayType;

public class FCSPortSerializer extends PortObjectSerializer <FCSPortObject> {
	
	private static final String ZIP_ENTRY_HEADER = "keywords";
	private static final String ZIP_ENTRY_PARAMETERLIST= "parameterList";
	private static final String ZIP_ENTRY_DATA= "data";

	@Override
	public void savePortObject(FCSPortObject FCSObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		ObjectMapper mapper = new ObjectMapper();
		// Serialize header
		Hashtable<String, String> header = FCSObject.getHeader();
		byte[] headerBytes = mapper.writeValueAsBytes(header);
		ZipEntry headerEntry = new ZipEntry(ZIP_ENTRY_HEADER);
		out.putNextEntry(headerEntry);
		out.write(headerBytes);
		
		// Serialize parameterList
		String[] parameterList = FCSObject.getParameterList();
		byte[] parameterListBytes = mapper.writeValueAsBytes(parameterList);
		ZipEntry pListEntry = new ZipEntry(ZIP_ENTRY_PARAMETERLIST);
		out.putNextEntry(pListEntry);
		out.write(parameterListBytes);
		
		// Serialize data
		Hashtable<String, double[]> parameterData = FCSObject.getData();
		byte[] parameterDataBytes = mapper.writeValueAsBytes(parameterData);
		ZipEntry parameterDataEntry = new ZipEntry(ZIP_ENTRY_DATA);
		out.putNextEntry(parameterDataEntry);
		out.write(parameterDataBytes);
		
	}

	@Override
	public FCSPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		Boolean stop = false;
		ZipEntry entry;
		String name;
		Hashtable<String, String> keywords = null;
		String [] parameterList = null;
		Hashtable<String, double[]> parameterData = null;
		FCSObjectSpec fcsSpec = null;
		ObjectMapper mapper = new ObjectMapper();
		while (stop==false){
			try {
				entry = in.getNextEntry();
				name = entry.getName();
				byte[] buffer = new byte[(int) entry.getCompressedSize()];
				if (name.equals(ZIP_ENTRY_HEADER)){
					in.read(buffer);
					MapLikeType type = mapper.getTypeFactory().constructMapLikeType(Hashtable.class, String.class, String.class);
					keywords = mapper.readValue(buffer, type);
				} else if (name.equals(ZIP_ENTRY_PARAMETERLIST)){
					in.read(buffer);
					ArrayType type = mapper.getTypeFactory().constructArrayType(String.class);
					parameterList = mapper.readValue(buffer, type);
				} else if (name.equals(ZIP_ENTRY_DATA)){
					in.read(buffer);
					ArrayType type = mapper.getTypeFactory().constructArrayType(double.class);
					parameterData = mapper.readValue(buffer, type);
				}
				
			} catch (NullPointerException e) {
					stop = true;
			}
			fcsSpec = new FCSObjectSpec(keywords,parameterList);		
		}	
		 if(keywords==null||parameterData==null||parameterList==null){
			 CanceledExecutionException e = new CanceledExecutionException("Null entries encountered when loading the port.");
			 e.printStackTrace();
			 throw e;
		 }
		FCSPortObject newFCSPortObject = new FCSPortObject(fcsSpec, parameterData);

		return newFCSPortObject;
	}
}
