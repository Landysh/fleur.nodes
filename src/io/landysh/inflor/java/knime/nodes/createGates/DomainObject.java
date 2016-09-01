package io.landysh.inflor.java.knime.nodes.createGates;

public interface DomainObject {
	public void   loadFromString(String objectString);
	public String saveToString();
	public String getUUID();
	public String getPrefferedName();
}
