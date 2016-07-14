package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class RangeGate extends AbstractGate {
	Hashtable<String, RangeDimension> dimensions;

	public RangeGate(String id) {
		super(id);
	}

	@Override
	public boolean[] evaluate(ConcurrentHashMap<String, double[]> data, int rowCount) {
		boolean[] finalResult = new boolean[rowCount];

		for (int i = 0; i < rowCount; i++) {
			boolean result = true;
			for (String name : dimensions.keySet()) {
				double value = data.get(name)[i];
				if (this.dimensions.get(name).evaluate(value) == false) {
					result = false;
					// if one then break.
					break;
				}
			}
			finalResult[i] = result;
		}
		return finalResult;
	}

	@Override
	public Element toXMLElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> getDimensionNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (String name : dimensions.keySet()) {
			names.add(name);
		}
		return names;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (dimensions.keySet() == null || dimensions.keySet().size() <= 1) {
			String message = "A range gate must have at least 1 dimension";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}
}
