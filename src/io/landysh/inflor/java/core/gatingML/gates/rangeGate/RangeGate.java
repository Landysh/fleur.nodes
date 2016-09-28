package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Hashtable;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class RangeGate extends AbstractGate {
	Hashtable<String, RangeDimension> dimensions;

	public RangeGate(String id) {
		super(id);
	}

	@Override
	public BitSet evaluate(ColumnStore data) {
		int rowCount = data.getRowCount();
		final BitSet finalResult = new BitSet(rowCount);

		for (int i = 0; i < rowCount; i++) {
			boolean result = true;
			for (final String name : dimensions.keySet()) {
				final double value = data.getDimensionData(name)[i];
				if (dimensions.get(name).evaluate(value) == false) {
					result = false;
					// if one fails then break.
					break;
				}
			}
			if (result == true){
				finalResult.set(i);
			}
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