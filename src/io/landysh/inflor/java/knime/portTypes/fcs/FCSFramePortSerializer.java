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

public class FCSFramePortSerializer extends PortObjectSerializer <FCSFramePortObject> {
	
	private static final String ZIP_ENTRY_HEADER = "keywords";
	private static final String ZIP_ENTRY_PARAMETERLIST= "parameterList";
	private static final String ZIP_ENTRY_DATA= "data";

	@Override
	public void savePortObject(FCSFramePortObject FCSObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		ObjectMapper mapper = new ObjectMapper();
		// Serialize header
		Hashtable<String, String> header = FCSObject.getHeader();
		byte[] headerBytes = mapper.writeValueAsBytes(header);
		ZipEntry headerEntry = new ZipEntry(ZIP_ENTRY_HEADER);
		//headerEntry.setSize(headerBytes.length);
		out.putNextEntry(headerEntry);
		out.write(headerBytes, 0, headerBytes.length);

		// Serialize parameterList
		String[] parameterList = FCSObject.getParameterList();
		byte[] parameterListBytes = mapper.writeValueAsBytes(parameterList);
		ZipEntry pListEntry = new ZipEntry(ZIP_ENTRY_PARAMETERLIST);
		//pListEntry.setSize(parameterListBytes.length);
		out.putNextEntry(pListEntry);
		out.write(parameterListBytes,0,parameterListBytes.length);

		// Serialize data
		Hashtable<String, double[]> parameterData = FCSObject.getData();
		byte[] parameterDataBytes = mapper.writeValueAsBytes(parameterData);
		ZipEntry parameterDataEntry = new ZipEntry(ZIP_ENTRY_DATA);
		//parameterDataEntry.setSize(parameterDataBytes.length);
		out.putNextEntry(parameterDataEntry);
		out.write(parameterDataBytes, 0, parameterDataBytes.length);		
	}

	@Override
	public FCSFramePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		Boolean stop = false;
		ZipEntry entry;
		String name;
		Hashtable<String, String> keywords = null;
		String [] parameterList = null;
		Hashtable<String, double[]> parameterData = null;
		FCSFrameSpec fcsSpec = null;
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
			fcsSpec = new FCSFrameSpec(keywords,parameterList);		
		}	
		 if(keywords==null||parameterData==null||parameterList==null){
			 CanceledExecutionException e = new CanceledExecutionException("Null entries encountered when loading the port.");
			 e.printStackTrace();
			 throw e;
		 }
		FCSFramePortObject newFCSPortObject = new FCSFramePortObject(fcsSpec, parameterData);

		return newFCSPortObject;
	}
}
