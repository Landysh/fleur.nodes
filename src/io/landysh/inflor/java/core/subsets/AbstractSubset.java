package io.landysh.inflor.java.core.subsets;

import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.DomainObject;

public abstract class AbstractSubset extends DomainObject {



	/**
	 * 
	 */
	private static final long serialVersionUID = -8536548751594402599L;

	protected BitSet members;
	
	public AbstractSubset() {
		super();
		members = null;
	}
	
	protected abstract BitSet evaluate();
	protected abstract ColumnStore getData();
}
//EOF