package io.landysh.inflor.java.core.gatingML.gates.booleanGate;

import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class BooleanGate extends AbstractGate {

	private BooleanOperator operator;
	private final ConcurrentHashMap<String, AbstractGate> references;

	public BooleanGate(String id) {
		super(id);
		references = new ConcurrentHashMap<String, AbstractGate>();
	}

	@Override
	public boolean[] evaluate(ConcurrentHashMap<String, double[]> data, int eventCount) {
		validate();
		final BooleanAccumulator acc = new BooleanAccumulator(operator);
		final boolean[] result = references.values().parallelStream().map(g -> g.evaluate(data, eventCount)).reduce(acc)
				.get();
		return result;
	}

	public BooleanOperator getBooleanOperator() {
		return operator;
	}

	public void setBooleanOperator(BooleanOperator op) {
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