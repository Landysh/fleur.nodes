package io.landysh.inflor.java.core.gatingML.compensation;

import static org.ejml.ops.CommonOps.invert;
import static org.ejml.ops.CommonOps.mult;

import java.util.HashMap;
import org.ejml.data.DenseMatrix64F;

import io.landysh.inflor.java.core.utils.FCSUtils;

public class SpilloverCompensator {

	// Compensation details
	public String[] compParameters;
	private Integer[] compParameterMap;
	private DenseMatrix64F compMatrix;
	private double[][] rawMatrix;

	public SpilloverCompensator(HashMap<String, String> keywords) {
		if (keywords.containsKey("$SPILL") || keywords.containsKey("SPILL")) {
			rawMatrix = parseSpillover(keywords);
			compMatrix = new DenseMatrix64F(rawMatrix);
			invert(compMatrix);
		}
	}

	// TODO: Unit test
	public double[] compensateRow(double[] FCSRow) {
		double[] compedRow = null;
		if (compParameters != null) {
			compedRow = new double[compParameters.length];
			final double[] unCompedRow = new double[compParameters.length];
			for (int i = 0; i < compParameters.length; i++) {
				final int index = compParameterMap[i];
				unCompedRow[i] = FCSRow[index];
			}
			final DenseMatrix64F unCompedVector = new DenseMatrix64F(new double[][] { unCompedRow });
			final DenseMatrix64F c = new DenseMatrix64F(new double[][] { unCompedRow });
			mult(unCompedVector, compMatrix, c);
			compedRow = c.data;
		}
		if (compedRow != null) {
			return compedRow;
		} else {
			final Exception e = new Exception("Comped array is null, this should not happen.");
			e.printStackTrace();
			return null;
		}
	}

	public String[] getCompParameterNames() {
		return compParameters;
	}

	public double[][] getMatrix() {
		return rawMatrix;
	}

	private double[][] parseSpillover(HashMap<String, String> keywords) {
		String spill = null;

		// Check for spillover keywords
		if (keywords.containsKey("$SPILLOVER")) {
			spill = keywords.get("SPILLOVER");
		} else if (keywords.containsKey("SPILL")) {
			spill = keywords.get("SPILL");
		} else {
			throw new NullPointerException("No spillover keyword found.");
		}

		// Magic string parsing from FCS Spec PDF
		final String[] s = spill.split(",");
		final int p = Integer.parseInt(s[0].trim());
		if (p >= 2) {
			final double[] spills = new double[p * p];
			int k = 0;
			for (int i = p + 1; i < spills.length + p + 1; i++) {
				spills[k] = Double.parseDouble(s[i]);
				k++;
			}
			final double[][] matrix = new double[p][p];
			final String[] compPars = new String[p];
			final Integer[] pMap = new Integer[p];
			for (int i = 0; i < compPars.length; i++) {
				compPars[i] = s[i + 1];
				final String parameterName = compPars[i];
				pMap[i] = FCSUtils.findParameterNumnberByName(keywords, parameterName);
				final double[] row = new double[p];
				for (int j = 0; j < p; j++) {
					final int index = 1 + p + i * p + j;
					row[j] = Double.parseDouble(s[index]);
				}
				matrix[i] = row;
			}
			compParameterMap = pMap;
			compParameters = compPars;
			return matrix;
		} else {
			return null;
		}
	}
}