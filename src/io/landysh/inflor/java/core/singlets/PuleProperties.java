package io.landysh.inflor.java.core.singlets;

public enum PuleProperties {
	AREA 	(new String[] {".+-A", ".+Area.+"}),
	HEIGHT 	(new String[] {".+-H", ".+Height.+"}),
	WIDTH 	(new String[] {".+-W", ".+Width.+"});
	private final String[] regi;
	PuleProperties (String[] regi){
		this.regi = regi;
	}
	public String[] regi() {return this.regi;}
}
