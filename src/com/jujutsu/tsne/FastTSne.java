package com.jujutsu.tsne;

import static org.ejml.ops.CommonOps.add;
import static org.ejml.ops.CommonOps.addEquals;
import static org.ejml.ops.CommonOps.divide;
import static org.ejml.ops.CommonOps.elementDiv;
import static org.ejml.ops.CommonOps.elementExp;
import static org.ejml.ops.CommonOps.elementLog;
import static org.ejml.ops.CommonOps.elementMult;
import static org.ejml.ops.CommonOps.elementPower;
import static org.ejml.ops.CommonOps.elementSum;
import static org.ejml.ops.CommonOps.mult;
import static org.ejml.ops.CommonOps.multAddTransB;
import static org.ejml.ops.CommonOps.scale;
import static org.ejml.ops.CommonOps.subtract;
import static org.ejml.ops.CommonOps.subtractEquals;
import static org.ejml.ops.CommonOps.sumCols;
import static org.ejml.ops.CommonOps.sumRows;
import static org.ejml.ops.CommonOps.transpose;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ejml.data.DenseMatrix64F;

/**
 *
 * Author: Leif Jonsson (leif.jonsson@gmail.com)
 * 
 * This is a port of van der Maaten and Hintons Python implementation of t-sne
 *
 */
public class FastTSne implements TSne {
	public static void addRowVector(DenseMatrix64F matrix, DenseMatrix64F rowvector) {
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				matrix.set(i, j, matrix.get(i, j) + rowvector.get(0, j));
			}
		}
	}

	/**
	 * All values in matrix that is less than <code>lessthan</code> is assigned
	 * the value <code>assign</code>
	 * 
	 * @param matrix
	 * @param lessthan
	 * @param assign
	 * @return
	 */
	public static void assignAllLessThan(DenseMatrix64F matrix, double lessthan, double assign) {
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				if (matrix.get(i, j) < lessthan) {
					matrix.set(i, j, assign);
				}
			}
		}
	}

	public static void assignAtIndex(DenseMatrix64F num, int[] range, int[] range1, double value) {
		for (int j = 0; j < range.length; j++) {
			num.set(range[j], range1[j], value);
		}
	}

	public static DenseMatrix64F colMean(DenseMatrix64F y, int i) {
		final DenseMatrix64F colmean = new DenseMatrix64F(1, y.numCols);
		sumCols(y, colmean);
		divide(colmean, y.numRows);
		return colmean;
	}

	public static double[][] extractDoubleArray(DenseMatrix64F p) {
		final int rows = p.getNumRows();
		final int cols = p.getNumCols();
		final double[][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = p.get(i, j);
			}
		}
		return result;
	}

	public static DenseMatrix64F fillWithRow(DenseMatrix64F matrix, int setrow) {
		final int rows = matrix.numRows;
		final int cols = matrix.numCols;
		final DenseMatrix64F result = new DenseMatrix64F(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				result.set(row, col, matrix.get(setrow, col));
			}
		}
		return result;
	}

	public static double[][] readBinaryDoubleMatrix(int rows, int columns, String fn)
			throws FileNotFoundException, IOException {
		final File matrixFile = new File(fn);
		final double[][] matrix = new double[rows][columns];
		try (DataInputStream dis = new DataInputStream(
				new BufferedInputStream(new FileInputStream(matrixFile.getAbsolutePath())))) {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					matrix[i][j] = dis.readDouble();
				}
			}
		}
		return matrix;
	}

	/**
	 * Replaces NaN's with repl
	 * 
	 * @param matrix
	 * @param repl
	 * @return
	 */
	public static void replaceNaN(DenseMatrix64F matrix, double repl) {
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				if (Double.isNaN(matrix.get(i, j))) {
					matrix.set(i, j, repl);
				}
			}
		}
	}

	public static DenseMatrix64F tile(DenseMatrix64F matrix, int rowtimes, int coltimes) {
		final DenseMatrix64F result = new DenseMatrix64F(matrix.numRows * rowtimes, matrix.numCols * coltimes);
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (int j = 0; j < matrix.numRows; j++) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix.numCols; l++) {
						result.set(resultrow, resultcol++, matrix.get(j, l));
					}
				}
				resultrow++;
			}
		}
		return result;
	}

	MatrixOps mo = new MatrixOps();

	/**
	 * Returns a new matrix of booleans where true is set if the value in the
	 * matrix is bigger than value
	 * 
	 * @param matrix
	 * @param value
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	boolean[][] biggerThan(DenseMatrix64F matrix, double value) {
		final boolean[][] equals = new boolean[matrix.numRows][matrix.numCols];
		for (int i = 0; i < matrix.numRows; i++) {
			for (int j = 0; j < matrix.numCols; j++) {
				equals[i][j] = Double.compare(matrix.get(i, j), value) == 1;
			}
		}
		return equals;
	}

	@Override
	public R Hbeta(double[][] D, double beta) {
		final DenseMatrix64F P = new DenseMatrix64F(D);
		scale(-beta, P);
		elementExp(P, P);
		final double sumP = elementSum(P); // sumP confirmed scalar
		final DenseMatrix64F Dd = new DenseMatrix64F(D);
		elementMult(Dd, P);
		final double H = Math.log(sumP) + beta * elementSum(Dd) / sumP;
		scale(1 / sumP, P);
		final R r = new R();
		r.H = H;
		r.P = extractDoubleArray(P);
		return r;
	}

	void maximize(DenseMatrix64F p, double minval) {
		final int rows = p.getNumRows();
		final int cols = p.getNumCols();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				final double val = p.get(i, j);
				if (val < minval)
					p.unsafe_set(j, j, minval);
			}
		}
	}

	/**
	 * <p>
	 * Sets the data of<code>target</code> to that of the input matrix with the
	 * values and shape defined by the 2D array 'data'. It is assumed that
	 * 'data' has a row-major formatting:<br>
	 * <br>
	 * data[ row ][ column ]
	 * </p>
	 * 
	 * @param target
	 *            2D DenseMatrix. Modified to contain the values in 'data'.
	 * @param data
	 *            2D array representation of the matrix. Not modified.
	 */
	public void setData(DenseMatrix64F target, double data[][]) {
		final int numRows = data.length;
		final int numCols = data[0].length;

		final double[] targetData = new double[numRows * numCols];

		int pos = 0;
		for (int i = 0; i < numRows; i++) {
			final double[] row = data[i];

			if (row.length != numCols) {
				throw new IllegalArgumentException("All rows must have the same length");
			}

			System.arraycopy(row, 0, targetData, pos, numCols);
			pos += numCols;
		}

		target.setData(targetData);
	}

	/**
	 * Sets the diagonal of 'diag' to the values of 'diagElements' as long as
	 * possible (i.e while there are elements left in diag and the dim of 'diag'
	 * is big enough... Note: This method ONLY affect the diagonal elements the
	 * others are left as when passed in.
	 * 
	 * @param diag
	 *            Modified to contain the elements of 'diagElements' on its
	 *            diagonal
	 * @param diagElems
	 */
	public void setDiag(DenseMatrix64F diag, double[] diagElems) {
		int idx = 0;
		while (idx < diag.numCols && idx < diag.numRows && idx < diagElems.length) {
			diag.set(idx, idx, diagElems[idx++]);
		}
	}

	@Override
	public double[][] tsne(double[][] X, int k, int initial_dims, double perplexity) {
		return tsne(X, k, initial_dims, perplexity, 2000, true);
	}

	@Override
	public double[][] tsne(double[][] X, int k, int initial_dims, double perplexity, int maxIterations) {
		return tsne(X, k, initial_dims, perplexity, maxIterations, true);
	}

	@Override
	public double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter,
			boolean use_pca) {
		System.out.println("X:Shape is = " + X.length + " x " + X[0].length);
		// Initialize variables
		if (use_pca && X[0].length > initial_dims) {
			final PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
			X = pca.pca(X, initial_dims);
			System.out.println("X:Shape after PCA is = " + X.length + " x " + X[0].length);
		}
		final int n = X.length;
		double momentum = .5;
		final double initial_momentum = 0.5;
		final double final_momentum = 0.8;
		final int eta = 500;
		final double min_gain = 0.01;
		final DenseMatrix64F Y = new DenseMatrix64F(mo.rnorm(n, no_dims));
		final DenseMatrix64F Ysqlmul = new DenseMatrix64F(Y.numRows, Y.numRows);
		final DenseMatrix64F dY = new DenseMatrix64F(mo.fillMatrix(n, no_dims, 0.0));
		final DenseMatrix64F iY = new DenseMatrix64F(mo.fillMatrix(n, no_dims, 0.0));
		final DenseMatrix64F gains = new DenseMatrix64F(mo.fillMatrix(n, no_dims, 1.0));
		final DenseMatrix64F btNeg = new DenseMatrix64F(n, no_dims);
		final DenseMatrix64F bt = new DenseMatrix64F(n, no_dims);

		// Compute P-values
		final DenseMatrix64F P = new DenseMatrix64F(x2p(X, 1e-5, perplexity).P); // P
		// =
		// n
		// x
		// n
		final DenseMatrix64F Ptr = new DenseMatrix64F(P.numRows, P.numCols);
		final DenseMatrix64F L = new DenseMatrix64F(P); // L = n x n
		final DenseMatrix64F logdivide = new DenseMatrix64F(P.numRows, P.numCols);
		final DenseMatrix64F diag = new DenseMatrix64F(mo.fillMatrix(L.numRows, L.numCols, 0.0));

		transpose(P, Ptr);
		addEquals(P, Ptr);
		divide(P, elementSum(P));
		replaceNaN(P, Double.MIN_VALUE);
		scale(4.0, P); // early exaggeration
		maximize(P, 1e-12);

		System.out.println("Y:Shape is = " + Y.getNumRows() + " x " + Y.getNumCols());

		final DenseMatrix64F sqed = new DenseMatrix64F(Y.numRows, Y.numCols);
		final DenseMatrix64F sum_Y = new DenseMatrix64F(1, Y.numRows);
		final DenseMatrix64F num = new DenseMatrix64F(Y.numRows, Y.numRows);
		final DenseMatrix64F Q = new DenseMatrix64F(P.numRows, P.numCols);

		for (int iter = 0; iter < max_iter; iter++) {
			// Compute pairwise affinities
			elementPower(Y, 2, sqed);
			sumRows(sqed, sum_Y);
			multAddTransB(-2.0, Y, Y, Ysqlmul);
			addRowVector(Ysqlmul, sum_Y);
			transpose(Ysqlmul);
			addRowVector(Ysqlmul, sum_Y);

			add(Ysqlmul, 1.0);
			divide(1.0, Ysqlmul);
			num.set(Ysqlmul);
			assignAtIndex(num, mo.range(n), mo.range(n), 0);
			divide(num, elementSum(num), Q);

			maximize(Q, 1e-12);

			// Compute gradient
			subtract(P, Q, L);
			elementMult(L, num);
			final DenseMatrix64F rowsum = sumRows(L, null); // rowsum = nx1
			final double[] rsum = new double[rowsum.numRows];
			for (int i = 0; i < rsum.length; i++) {
				rsum[i] = rowsum.get(i, 0);
			}
			setDiag(diag, rsum);
			subtract(diag, L, L);
			mult(L, Y, dY);
			scale(4.0, dY);

			// Perform the update
			if (iter < 20)
				momentum = initial_momentum;
			else
				momentum = final_momentum;

			final boolean[][] boolMtrx = mo.equal(biggerThan(dY, 0.0), biggerThan(iY, 0.0));

			setData(btNeg, mo.abs(mo.negate(boolMtrx)));
			setData(bt, mo.abs(boolMtrx));

			final DenseMatrix64F gainsSmall = new DenseMatrix64F(gains);
			final DenseMatrix64F gainsBig = new DenseMatrix64F(gains);
			add(gainsSmall, 0.2);
			scale(0.8, gainsBig);

			elementMult(gainsSmall, btNeg);
			elementMult(gainsBig, bt);
			add(gainsSmall, gainsBig, gains);

			assignAllLessThan(gains, min_gain, min_gain);

			scale(momentum, iY);
			final DenseMatrix64F gainsdY = new DenseMatrix64F(gains.numRows, dY.numCols);
			elementMult(gains, dY, gainsdY);
			scale(eta, gainsdY);
			subtractEquals(iY, gainsdY);
			addEquals(Y, iY);
			final DenseMatrix64F colMeanY = colMean(Y, 0);
			final DenseMatrix64F meanTile = tile(colMeanY, n, 1);
			subtractEquals(Y, meanTile);

			// Compute current value of cost function
			if (iter % 100 == 0) {
				final DenseMatrix64F Pdiv = new DenseMatrix64F(P);
				elementDiv(Pdiv, Q);
				elementLog(Pdiv, logdivide);
				replaceNaN(logdivide, Double.MIN_VALUE);
				elementMult(logdivide, P);
				replaceNaN(logdivide, Double.MIN_VALUE);
				final double C = elementSum(logdivide);
				System.out.println("Iteration " + iter + ": error is " + C);
			} else if (iter % 10 == 0) {
				System.out.println("Iteration " + iter);
			}

			// Stop lying about P-values
			if (iter == 100)
				divide(P, 4);
		}

		// Return solution
		return extractDoubleArray(Y);
	}

	@Override
	public R x2p(double[][] X, double tol, double perplexity) {
		final int n = X.length;
		final double[][] sum_X = mo.sum(mo.square(X), 1);
		final double[][] times = mo.scalarMult(mo.times(X, mo.transpose(X)), -2);
		final double[][] prodSum = mo.addColumnVector(mo.transpose(times), sum_X);
		final double[][] D = mo.addRowVector(prodSum, mo.transpose(sum_X));
		// D seems correct at this point compared to Python version
		final double[][] P = mo.fillMatrix(n, n, 0.0);
		final double[] beta = mo.fillMatrix(n, n, 1.0)[0];
		final double logU = Math.log(perplexity);
		System.out.println("Starting x2p...");
		for (int i = 0; i < n; i++) {
			if (i % 500 == 0)
				System.out.println("Computing P-values for point " + i + " of " + n + "...");
			double betamin = Double.NEGATIVE_INFINITY;
			double betamax = Double.POSITIVE_INFINITY;
			final double[][] Di = mo.getValuesFromRow(D, i, mo.concatenate(mo.range(0, i), mo.range(i + 1, n)));

			R hbeta = Hbeta(Di, beta[i]);
			double H = hbeta.H;
			double[][] thisP = hbeta.P;

			// Evaluate whether the perplexity is within tolerance
			double Hdiff = H - logU;
			int tries = 0;
			while (Math.abs(Hdiff) > tol && tries < 50) {
				if (Hdiff > 0) {
					betamin = beta[i];
					if (Double.isInfinite(betamax))
						beta[i] = beta[i] * 2;
					else
						beta[i] = (beta[i] + betamax) / 2;
				} else {
					betamax = beta[i];
					if (Double.isInfinite(betamin))
						beta[i] = beta[i] / 2;
					else
						beta[i] = (beta[i] + betamin) / 2;
				}

				hbeta = Hbeta(Di, beta[i]);
				H = hbeta.H;
				thisP = hbeta.P;
				Hdiff = H - logU;
				tries = tries + 1;
			}
			mo.assignValuesToRow(P, i, mo.concatenate(mo.range(0, i), mo.range(i + 1, n)), thisP[0]);
		}

		final R r = new R();
		r.P = P;
		r.beta = beta;
		final double sigma = mo.mean(mo.sqrt(mo.scalarInverse(beta)));

		System.out.println("Mean value of sigma: " + sigma);

		return r;
	}
}
