package io.landysh.inflor.java.core.dataStructures;

public interface DomainObject {
	public String getPrefferedName();

	public String getUUID();

	public void loadFromString(String objectString);

	public String saveToString();
}
