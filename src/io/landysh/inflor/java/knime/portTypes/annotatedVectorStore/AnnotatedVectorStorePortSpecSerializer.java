package io.landysh.inflor.java.knime.portTypes.annotatedVectorStore;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.ZipEntry;

import org.knime.core.node.port.PortObjectSpec.PortObjectSpecSerializer;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto;
import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Builder;
import io.landysh.inflor.java.core.proto.AnnotatedVectorMessage.AnnotatedVectorsProto.Keyword;


public class AnnotatedVectorStorePortSpecSerializer extends PortObjectSpecSerializer<AnnotatedVectorStoreSpec> {

	private static final String ZIP_ENTRY_NAME = "AVSMessage";

	@Override
	public AnnotatedVectorStoreSpec loadPortObjectSpec(PortObjectSpecZipInputStream in) throws IOException {
		AnnotatedVectorStoreSpec fcsSpec = null;
		ZipEntry entry=null;
		try {
			entry = in.getNextEntry();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		if (entry.getName().equals(ZIP_ENTRY_NAME)){
			//Do the Vector names.
			//will break for files greater than int.max bytes in size?
			byte[] buffer = new byte[(int) entry.getSize()];
			in.read(buffer);
			AnnotatedVectorsProto message =AnnotatedVectorsProto.parseFrom(buffer);
			String[] parameterList = (String[]) message.getVectorNamesList().toArray();
			//Do the keywords
			Hashtable <String, String> keywords = new Hashtable<String, String>();
			for (int i=0;i<message.getKeywordsCount();i++){
				Keyword keyword = message.getKeywords(i);
				String key = keyword.getKey();
				String value = keyword.getValue();
				keywords.put(key, value);
			}
			
			fcsSpec = new AnnotatedVectorStoreSpec(keywords, parameterList);
			
		} else {
			IOException e = new IOException("Invalid Zip entry name.");
			e.printStackTrace();
			throw e;
		}
		return fcsSpec;
	}

	@Override
	public void savePortObjectSpec(AnnotatedVectorStoreSpec portObjectSpec, PortObjectSpecZipOutputStream out) throws IOException {
		/**
		 * Creates a protobuf of an AVS Messages without vector data, and writes to ZipOutPutStream.
		 */
		
		//create the builder
		Builder messageBuilder = AnnotatedVectorsProto.newBuilder();
		
		//add the vector names.
		for (String s: portObjectSpec.parameterList){
			messageBuilder.addVectorNames(s);
		}
		
		//add the keywords.
		for (String s: portObjectSpec.getHeader().keySet()){
			messageBuilder.addKeywords(Keyword.newBuilder()
		   									  .setKey(s)
											  .setValue(portObjectSpec.getHeader().get(s))
										 	  .build()
											  );
			}
		//build the message
		AnnotatedVectorsProto avSpec = messageBuilder.build();
		
		//write the message in a zip entry in the output stream.
		ZipEntry avsEntry = new ZipEntry(ZIP_ENTRY_NAME);
		out.putNextEntry(avsEntry);
		avSpec.writeTo(out);
		out.closeEntry();
	}
}