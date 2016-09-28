package io.landysh.inflor.java.core.gatingML.gates;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RangeGate;

public abstract class AbstractGate {
	/**
	 * An abstract class that provides the basis for all gating-ml compliant
	 * gates.
	 * 
	 * @see RangeGate
	 */

	protected String id;
	protected String parent_id;
	protected Hashtable<String, String> custom_info;

	public AbstractGate(final String id) {
		this.id = id;
	}

	/**
	 * 
	 * @param data
	 *            - The input data. Must contain entries for all of
	 *            this.getDimensionNames().
	 * @param eventCount
	 *            - the number of events in each column
	 * @return A boolean array of rowCount.length where true corresponds to
	 *         being in the gate
	 */
	public abstract BitSet evaluate(ColumnStore data);

	public String getId() {
		return id;
	}

	public String getInfo(String name) {
		return custom_info.get(name);
	}

	/**
	 * Used to implement hierarchical gating.
	 * 
	 * @return the id of the gate upon which this gate depends.
	 */
	public String getParent_id() {
		return parent_id;
	}

	public String setInfo(String key, String value) {
		return custom_info.put(key, value);
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	/**
	 * Generate a gating-ML 2.0 compliant XML Element for this gate.
	 * 
	 * @return an org.w3c.dom Element.
	 */
	public abstract Element toXMLElement();

	/**
	 * Override to validate the gate definition. If gate is invalid, throw an
	 * exception.
	 * 
	 * @throws IllegalStateException
	 */
	public abstract void validate() throws IllegalStateException;
}
