package io.landysh.inflor.java.core.gatingML.gates.booleanGate;

import java.util.concurrent.ConcurrentHashMap;
import org.w3c.dom.Element;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class BooleanGate extends AbstractGate {

	private BooleanOperator operator;
	private ConcurrentHashMap<String, AbstractGate> references;

	public BooleanGate(String id) {
		super(id);
		this.references = new ConcurrentHashMap<String, AbstractGate>();
	}

	@Override
	public boolean[] evaluate(ConcurrentHashMap<String, double[]> data, int eventCount) {
		validate();
		BooleanAccumulator acc = new BooleanAccumulator(this.operator);
		boolean[] result = references.values().parallelStream().map(g -> g.evaluate(data, eventCount)).reduce(acc)
				.get();
		return result;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (operator != null) {
			String message = "A boolean operator must be selected.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}

		if (this.references.size() > 2) {
			String message = "A boolean gate must reference at least 2 other gates.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	@Override
	public Element toXMLElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public BooleanOperator getBooleanOperator() {
		return this.operator;
	}

	public void setBooleanOperator(BooleanOperator op) {
		this.operator = op;
	}
}