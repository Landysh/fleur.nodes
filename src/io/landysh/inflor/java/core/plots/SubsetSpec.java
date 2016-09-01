package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import io.landysh.inflor.java.core.dataStructures.DomainObject;

public class SubsetSpec implements DomainObject {
	public final String uuid;
	String displayName;

	public SubsetSpec(String priorUUID) {
		// Create new UUID if needed.
		if (priorUUID == null) {
			uuid = UUID.randomUUID().toString();
		} else {
			uuid = priorUUID;
		}
	}

	@Override
	public String getPrefferedName() {
		if (displayName == null) {
			return uuid;
		} else {
			return displayName;
		}
	}

	@Override
	public String getUUID() {
		return uuid;
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
}
