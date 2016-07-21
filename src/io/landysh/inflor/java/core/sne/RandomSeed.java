package io.landysh.inflor.java.core.sne;

public enum RandomSeed {
	STATIC (new Long(42)), 
	CURRENT_TIME(new Long(System.currentTimeMillis()));
	
	private final Long value;

	RandomSeed(Long value) {
		this.value = value;
	}

	public Long value() {
		return this.value;
	}
	
}
