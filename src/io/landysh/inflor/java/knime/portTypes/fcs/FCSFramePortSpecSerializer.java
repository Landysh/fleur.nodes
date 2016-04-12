package io.landysh.inflor.java.knime.portTypes.fcs;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;

import org.knime.core.node.port.PortObjectSpec.PortObjectSpecSerializer;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.MapLikeType;

public class FCSFramePortSpecSerializer extends PortObjectSpecSerializer<FCSFrameSpec> {

	private static final String ZIP_ENTRY_HEADER = "keywords";
	private static final String ZIP_ENTRY_PARAMETERLIST= "parameterList";

	@Override
	public FCSFrameSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) {
		Boolean stop = false;
		ZipEntry entry;
		String name;
		Hashtable<String, String> keywords = null;
		String [] parameterList = null;
		FCSFrameSpec fcsSpec = null;
		ObjectMapper mapper = new ObjectMapper();
		while (stop==false){
			try {
				entry = in.getNextEntry();
				name = entry.getName();
				Integer size = (int) entry.getCompressedSize();
				byte[] buffer = new byte[size];
				if (name.equals(ZIP_ENTRY_HEADER)){
					in.read(buffer);
					MapLikeType type = mapper.getTypeFactory().constructMapLikeType(Hashtable.class, String.class, String.class);
					keywords = mapper.readValue(buffer, type);
				} else if (name.equals(ZIP_ENTRY_PARAMETERLIST)){
					in.read(buffer);
					ArrayType type = mapper.getTypeFactory().constructArrayType(String.class);
					parameterList = mapper.readValue(buffer, type);
				} else {
					System.out.print("Houston");
				}
			} catch (Exception e) {
				e.printStackTrace();
				stop=true;			}
			fcsSpec = new FCSFrameSpec(keywords,parameterList);		
		}	
		return fcsSpec;
	}

	@Override
	public void savePortObjectSpec(FCSFrameSpec portObjectSpec, PortObjectSpecZipOutputStream out) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		// Serialize header
		Hashtable<String, String> header = portObjectSpec.getHeader();
		byte[] headerBytes = mapper.writeValueAsBytes(header);
		ZipEntry headerEntry = new ZipEntry(ZIP_ENTRY_HEADER);
		//headerEntry.setSize(headerBytes.length);
		out.putNextEntry(headerEntry);
		out.write(headerBytes,0,headerBytes.length);
		
		// Serialize parameterList
		String[] parameterList = portObjectSpec.getParameterList();
		byte[] parameterListBytes = mapper.writeValueAsBytes(parameterList);
		ZipEntry pListEntry = new ZipEntry(ZIP_ENTRY_PARAMETERLIST);
		//pListEntry.setSize(parameterListBytes.length);
		out.putNextEntry(pListEntry);
		out.write(parameterListBytes,0,headerBytes.length);
		
	}

}
