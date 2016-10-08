package io.landysh.inflor.java.core.gatingML.gates.booleanGate;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.gatingML.gates.BitSetAccumulator;
import io.landysh.inflor.java.core.gatingML.gates.BitSetOperator;

public class BooleanGate extends AbstractGate {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7315903940971172599L;
	private BitSetOperator operator;
	private final ConcurrentHashMap<String, AbstractGate> references;

	public BooleanGate(String id) {
		super(id);
		references = new ConcurrentHashMap<String, AbstractGate>();
	}

	@Override
	public BitSet evaluate(ColumnStore data) {
		validate();
		final BitSetAccumulator acc = new BitSetAccumulator(operator);
		final BitSet result = references.values().parallelStream().map(g -> g.evaluate(data)).reduce(acc).get();
		return result;
	}

	public BitSetOperator getBooleanOperator() {
		return operator;
	}

	public void setBooleanOperator(BitSetOperator op) {
		operator = op;
	}

	@Override
	public Element toXMLElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (operator != null) {
			final String message = "A boolean operator must be selected.";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}

		if (references.size() > 2) {
			final String message = "A boolean gate must reference at least 2 other gates.";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}
}