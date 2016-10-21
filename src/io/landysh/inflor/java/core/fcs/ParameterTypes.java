package io.landysh.inflor.java.core.fcs;

public enum ParameterTypes {
	SCATTER(new String[] { ".*scatter.*", ".*fsc.*", ".*fcs.*", ".*ssc.*", ".*orth.*" }), 
	VIABILITY(new String[] { }), 
	TIME(new String[] { ".*time.*"}),
	DNA(new String[] { ".*dapi.*", ".*pi.*"});

	private final String[] regi;

	ParameterTypes(String[] regi) {
			this.regi = regi;
	}

	public String[] regi() {
			return regi;
	}
}
