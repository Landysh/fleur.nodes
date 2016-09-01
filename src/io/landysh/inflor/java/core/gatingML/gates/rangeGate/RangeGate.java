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
		final boolean[] finalResult = new boolean[rowCount];

		for (int i = 0; i < rowCount; i++) {
			boolean result = true;
			for (final String name : dimensions.keySet()) {
				final double value = data.get(name)[i];
				if (dimensions.get(name).evaluate(value) == false) {
					result = false;
					// if one then break.
					break;
				}
			}
			finalResult[i] = result;
		}
		return finalResult;
	}

	public ArrayList<String> getDimensionNames() {
		final ArrayList<String> names = new ArrayList<String>();
		for (final String name : dimensions.keySet()) {
			names.add(name);
		}
		return names;
	}

	@Override
	public Element toXMLElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (dimensions.keySet() == null || dimensions.keySet().size() <= 1) {
			final String message = "A range gate must have at least 1 dimension";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}
}
// EOF