package io.landysh.inflor.java.core.plots;

import io.landysh.inflor.java.core.dataStructures.DomainObject;

public class SubsetSpec implements DomainObject {
	public final String UUID;
	String displayName;

	public SubsetSpec(String priorUUID) {
		// Create new UUID if needed.
		if (priorUUID == null) {
			this.UUID = java.util.UUID.randomUUID().toString();
		} else {
			UUID = priorUUID;
		}
	}

	public SubsetSpec() {
		this(null);
	}

	@Override
	public String getPrefferedName() {
		if (displayName == null) {
			return UUID;
		} else {
			return displayName;
		}
	}

	@Override
	public String getUUID() {
		return UUID;
	}

	@Override
	public void loadFromString(String objectString) {
		// TODO Auto-generated method stub
	}

	@Override
	public String saveToString() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPrefferedName(String newName) {
		displayName = newName;
	}
}
