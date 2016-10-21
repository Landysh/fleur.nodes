package io.landysh.inflor.java.core.subsets;

import java.io.Serializable;
import java.util.BitSet;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;

public class RootSubset extends AbstractSubset implements Serializable{

	/**
	 *  Root subset class. Used as a root node in a subset tree.
	 */
	private static final long serialVersionUID = -8189764506384264612L;
	private static final String ROOT_NAME = "Ungated";
	private ColumnStore data;
	
	public RootSubset(ColumnStore data) {
		this(null, data);
	}
	
	public RootSubset(String priorUUID, ColumnStore data) {
		super(priorUUID);
		this.data = data;
		this.members = new BitSet(data.getRowCount());
	}

	@Override
	protected BitSet evaluate() {
		return this.members;
	}

	@Override
	public ColumnStore getData() {
		return this.data;
	}
	
	@Override
	public String toString(){
		return ROOT_NAME;
	}
	
}
//EOF