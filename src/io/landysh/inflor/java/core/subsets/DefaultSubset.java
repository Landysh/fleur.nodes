package io.landysh.inflor.java.core.subsets;

import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.utils.FCSUtils;

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
		FCSFrame data = getData();
		members = gate.evaluate(data);
		return null;
	}

	@Override
	public FCSFrame getData() {	
		FCSFrame data = parentSubset.getData();
		BitSet mask = gate.evaluate(data);
		FCSFrame filteredData = FCSUtils.filterColumnStore(mask, data);
		return filteredData;
	}
}
