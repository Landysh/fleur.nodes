package io.landysh.inflor.java.core.gatingML.compensation;

import static org.ejml.ops.CommonOps.invert;
import static org.ejml.ops.CommonOps.mult;

import java.util.Hashtable;

import org.ejml.data.DenseMatrix64F;

import io.landysh.inflor.java.core.utils.FCSUtils;

public class SpilloverCompensator {

	// Compensation details
	public  String[] compParameters;
	private Integer[] compParameterMap;
	private DenseMatrix64F compMatrix;
	private double[][] rawMatrix;
	public  SpilloverCompensator(Hashtable<String, String> keywords) throws Exception {
		if (keywords.containsKey("$SPILL") || keywords.containsKey("SPILL")) {
			rawMatrix = parseSpillover(keywords);
			compMatrix = new DenseMatrix64F(rawMatrix);
			invert(compMatrix);
		}
	}

	private double[][] parseSpillover(Hashtable<String, String> keywords) throws Exception {
		String spill = null;

		// Check for spillover keywords
		if (keywords.containsKey("$SPILLOVER")) {
			spill = keywords.get("SPILLOVER");
		} else if (keywords.containsKey("SPILL")) {
			spill = keywords.get("SPILL");
		} else {
			throw new Exception("No spillover keyword found.");
		}

		// Magic string parsing from FCS Spec PDF
		String[] s = spill.split(",");
		int p = Integer.parseInt(s[0].trim());
		if (p >= 2) {
			double[] spills = new double[p * p];
			int k = 0;
			for (int i = p + 1; i < spills.length + p + 1; i++) {
				spills[k] = Double.parseDouble(s[i]);
				k++;
			}
			double[][] matrix = new double[p][p];
			String[] compPars = new String[p];
			Integer[] pMap = new Integer[p];
			for (int i = 0; i < compPars.length; i++) {
				compPars[i] = s[i + 1];
				String parameterName = compPars[i];
				pMap[i] = FCSUtils.findParameterNumnberByName(keywords, parameterName);
				double[] row = new double[p];
				for (int j = 0; j < p; j++) {
					int index = 1 + p + i * p + j;
					row[j] = Double.parseDouble(s[index]);
				}
				matrix[i] = row;
			}
			compParameterMap = pMap;
			compParameters = compPars;
			return matrix;
		} else {
			throw new Exception("Spillover Keyword - " + spill + " - appears to be invalid.");
		}
	}

	// TODO: Unit test
	public double[] compensateRow(double[] FCSRow) {
		double[] compedRow = null;
		if (compParameters != null) {
			compedRow = new double[compParameters.length];
			double[] unCompedRow = new double[compParameters.length];
			for (int i = 0; i < compParameters.length; i++) {
				int index = compParameterMap[i];
				unCompedRow[i] = FCSRow[index];
			}
			DenseMatrix64F unCompedVector = new DenseMatrix64F(new double[][] { unCompedRow });
			DenseMatrix64F c = new DenseMatrix64F(new double[][] { unCompedRow });
			mult(unCompedVector, compMatrix, c);
			compedRow = c.data;
		}
		if (compedRow != null) {
			return compedRow;
		} else {
			Exception e = new Exception("Comped array is null, this should not happen.");
			e.printStackTrace();
			return null;
		}
	}

	public String[] getCompParameterNames() {
		return compParameters;
	}

	// TODO: Unit test
	public String[] getCompDisplayNames(Hashtable<String, String> keywords) {
		if (compParameters != null) {
			String[] displayNames = new String[compParameters.length];
			for (int i = 0; i < displayNames.length; i++) {
				displayNames[i] = FCSUtils.getDisplayName(keywords, compParameters[i], true);
			}
			return displayNames;
		} else {
			return new String[] {};
		}
	}

	public double[][] getMatrix() {
		return rawMatrix;
	}
}