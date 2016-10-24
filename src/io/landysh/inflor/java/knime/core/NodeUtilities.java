package io.landysh.inflor.java.knime.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class NodeUtilities {
	@SuppressWarnings("unchecked")
	public static HashMap<String, Serializable> loadHashMap(NodeSettingsRO settings, String key) throws InvalidSettingsException {		
		try {
			byte[] chartBytes = settings.getByteArray(key);
			ByteArrayInputStream bis = new ByteArrayInputStream(chartBytes);
			ObjectInputStream ois;
			ois = new ObjectInputStream(bis);
			HashMap<String, Serializable> loadedObject = (HashMap<String, Serializable>) ois.readObject();
			return loadedObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidSettingsException("Unable to parse map object");
		}
	}

	public static void saveSerializable(NodeSettingsWO settings, String key, Serializable obj)  {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] chartBytes = bos.toByteArray();
		settings.addByteArray(key, chartBytes);
	}
}
