package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;

import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto;
import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Builder;
import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Keyword;
import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Vector;

public class AnnotatedVectorStorePortSerializer extends PortObjectSerializer <AnnotatedVectorStorePortObject> {
	
	private static final String ZIP_ENTRY_NAME = "AVSMessage";

	@Override
	public void savePortObject(AnnotatedVectorStorePortObject AVSObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		/**
		 * Creates a protobuf of an AVS Messages without vector data, and writes to ZipOutPutStream.
		 */
		
		//create the builder
		Builder messageBuilder = AnnotatedVectorsProto.newBuilder();
		
		//add the vector names.
		for (String s: AVSObject.parameterList){
			messageBuilder.addVectorNames(s);
		}
		
		//add the keywords.
		for (String s: AVSObject.getHeader().keySet()){
			messageBuilder.addKeywords(Keyword.newBuilder()
		   									  .setKey(s)
											  .setValue(AVSObject.getHeader().get(s))
										 	  .build()
											  );
			}
		//add the data.
		
		for (int i=0;i<AVSObject.parameterList.length;i++){
			io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Vector.Builder vectorBuilder = Vector.newBuilder();
			Double[] vectorArray = AVSObject.getData().get(AVSObject.parameterList[i]);
			
			vectorBuilder.setName(AVSObject.parameterList[i]);
			for (int j=0;j<vectorArray.length;j++){
				vectorBuilder.addArray(vectorArray[j]);
			}
			Vector v = vectorBuilder.build();
			messageBuilder.addVectors(v);
			}
		
		//build the message
		AnnotatedVectorsProto avSpec = messageBuilder.build();
		
		//write the message in a zip entry in the output stream.
		ZipEntry avsEntry = new ZipEntry(ZIP_ENTRY_NAME);
		out.putNextEntry(avsEntry);
		avSpec.writeTo(out);
		out.closeEntry();

	}

	@Override
	public AnnotatedVectorStorePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		
		AnnotatedVectorStorePortObject vectorStorePort;
		ZipEntry entry=null;
		try {
			entry = in.getNextEntry();
		
			if (entry.getName().equals(ZIP_ENTRY_NAME)){
				//Do the Vector names.
				//will break for files greater than int.max bytes in size?
				byte[] buffer = new byte[(int) entry.getSize()];
				in.read(buffer);
				AnnotatedVectorsProto message =AnnotatedVectorsProto.parseFrom(buffer);
				Hashtable <String, Double[]> data = new Hashtable<String, Double[]>();	
				for (int i=0;i<message.getVectorsCount();i++){
					Vector vector = message.getVectors(i);
					String   key = vector.getName();
					Double[] values = (Double[]) vector.getArrayList().toArray();
					data.put(key, values);
				}
			vectorStorePort = new AnnotatedVectorStorePortObject((AnnotatedVectorStoreSpec) spec, data);
			return vectorStorePort;
			
		} else {
			IOException e = new IOException("Invalid Zip entry name.");
			e.printStackTrace();
			throw e;
		}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
