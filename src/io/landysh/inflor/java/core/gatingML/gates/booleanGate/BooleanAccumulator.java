package io.landysh.inflor.java.core.gatingML.gates.booleanGate;

import java.util.function.BinaryOperator;

public class BooleanAccumulator implements BinaryOperator<boolean[]> {

	BooleanOperator operator;

	public BooleanAccumulator(BooleanOperator op) {
		this.operator = op;
	}

	@Override
	public boolean[] apply(boolean[] t, boolean[] u) {

		if (operator.equals(BooleanOperator.NOT) == true) {
			return applyNOT(t, u);

		} else if (operator.equals(BooleanOperator.AND) == true) {
			return applyAND(t, u);

		} else {
			return applyOR(t, u);
		}
	}

	private boolean[] applyOR(boolean[] t, boolean[] u) {
		boolean[] result = new boolean[t.length];
		for (int i = 0; i < t.length; i++) {
			if (t[i] == true || u[i] == true) {
				result[i] = true;
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	private boolean[] applyAND(boolean[] t, boolean[] u) {
		boolean[] result = new boolean[t.length];
		for (int i = 0; i < t.length; i++) {
			if (t[i] == true && u[i] == true) {
				result[i] = true;
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	private boolean[] applyNOT(boolean[] t, boolean[] u) {
		boolean[] result = new boolean[t.length];
		for (int i = 0; i < t.length; i++) {
			if (t[i] == true || u[i] == true) {
				result[i] = false;
			} else {
				result[i] = true;
			}
		}
		return result;
	}
}