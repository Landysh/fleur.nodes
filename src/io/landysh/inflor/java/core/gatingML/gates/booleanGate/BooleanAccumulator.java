package io.landysh.inflor.java.core.gatingML.gates.booleanGate;

import java.util.BitSet;
import java.util.function.BinaryOperator;

public class BooleanAccumulator implements BinaryOperator<BitSet> {

	BooleanOperator operator;

	public BooleanAccumulator(BooleanOperator op) {
		operator = op;
	}

	@Override
	public BitSet apply(BitSet t, BitSet u) {

		if (operator.equals(BooleanOperator.NOT) == true) {
			t.andNot(u);
			return t;

		} else if (operator.equals(BooleanOperator.AND) == true) {
			t.and(u);
			return t;

		} else {
			t.or(u);
			return t;
		}
	}
}