package io.landysh.inflor.java.core.subsets;

import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class DefaultSubset extends AbstractSubset {

	/**
	 *  A lightweight and serializable subset definition
	 */
	private static final long serialVersionUID = 3267152042593062193L;

	AbstractSubset parentSubset;
	AbstractGate gate;
	
	public DefaultSubset(AbstractSubset parent, AbstractGate filter) {
		this(null, parent, filter);
	}

	public DefaultSubset(String priorUUID, AbstractSubset parent, AbstractGate filter) {
		super(priorUUID);
		parentSubset = parent;
		gate = filter;
	}

	@Override
	protected BitSet evaluate() {
		ColumnStore data = getData();
		members = gate.evaluate(data);
		return null;
	}

	@Override
	protected ColumnStore getData() {	
		return parentSubset.getData();
	}
	
}
