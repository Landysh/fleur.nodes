package io.landysh.inflor.java.core.gatingML.gates;

import java.util.BitSet;
import java.util.function.BinaryOperator;

public class BitSetAccumulator implements BinaryOperator<BitSet> {

	BitSetOperator operator;

	public BitSetAccumulator(BitSetOperator op) {
		operator = op;
	}

	@Override
	public BitSet apply(BitSet t, BitSet u) {

		if (operator.equals(BitSetOperator.NOT) == true) {
			t.andNot(u);
			return t;

		} else if (operator.equals(BitSetOperator.AND) == true) {
			t.and(u);
			return t;

		} else {
			t.or(u);
			return t;
		}
	}
}