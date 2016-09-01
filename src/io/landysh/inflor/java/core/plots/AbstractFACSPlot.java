package io.landysh.inflor.java.core.plots;

import java.util.UUID;

import org.w3c.dom.Document;

public abstract class AbstractFACSPlot {

	public static AbstractFACSPlot load(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	final String uuid;

	public AbstractFACSPlot() {
		uuid = UUID.randomUUID().toString();
	}

	public String getId() {
		return uuid;
	}

	public abstract Document toXML();
}
