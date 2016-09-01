package com.jujutsu.tsne;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

import Jama.Matrix;

public class MatrixOps {
	public interface MatrixOp {
		double compute(double op1, double op2);
	}

	class MatrixOperator extends RecursiveAction {
		final static long serialVersionUID = 1L;
		double[][] matrix1;
		double[][] matrix2;
		double[][] resultMatrix;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;
		MatrixOp op;

		public MatrixOperator(double[][] matrix1, double[][] matrix2, double[][] resultMatrix, MatrixOp op,
				int startRow, int endRow, int ll) {
			this.op = op;
			limit = ll;
			this.matrix1 = matrix1;
			this.matrix2 = matrix2;
			this.resultMatrix = resultMatrix;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ((endRow - startRow) <= limit) {
					final int cols = matrix1[0].length;
					for (int i = startRow; i < endRow; i++) {
						for (int j = 0; j < cols; j++) {
							resultMatrix[i][j] = op.compute(matrix1[i][j], matrix2[i][j]);
						}
					}
				} else {
					final int range = (endRow - startRow);
					final int startRow1 = startRow;
					final int endRow1 = startRow + (range / 2);
					final int startRow2 = endRow1;
					final int endRow2 = endRow;
					invokeAll(new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow1, endRow1, limit),
							new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow2, endRow2, limit));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	class MatrixTransposer extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		double[][] orig;
		double[][] transpose;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;

		public MatrixTransposer(double[][] orig, double[][] transpose, int startRow, int endRow) {
			this.orig = orig;
			this.transpose = transpose;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		public MatrixTransposer(double[][] orig, double[][] transpose, int startRow, int endRow, int ll) {
			limit = ll;
			this.orig = orig;
			this.transpose = transpose;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ((endRow - startRow) <= limit) {
					final int cols = orig[0].length;
					for (int i = 0; i < cols; i++) {
						for (int j = startRow; j < endRow; j++) {
							transpose[i][j] = orig[j][i];
						}
					}
				} else {
					final int range = (endRow - startRow);
					final int startRow1 = startRow;
					final int endRow1 = startRow + (range / 2);
					final int startRow2 = endRow1;
					final int endRow2 = endRow;
					invokeAll(new MatrixTransposer(orig, transpose, startRow1, endRow1, limit),
							new MatrixTransposer(orig, transpose, startRow2, endRow2, limit));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static ForkJoinPool pool = new ForkJoinPool();

	/**
	 * This function returns a new matrix which is centered and scaled, i.e each
	 * the mean is subtracted from each element in the matrix and divided by the
	 * matrix standard deviation
	 * 
	 * @param mu
	 * @param sigma
	 * @return new matrix which is centered (subtracted mean) and scaled
	 *         (divided with stddev)
	 */
	public static double[][] centerAndScale(double[][] matrix) {
		final double[][] res = new double[matrix.length][matrix[0].length];
		final double mean = mean(matrix);
		final double std = stdev(matrix);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				res[i][j] = (matrix[i][j] - mean) / std;
			}
		}

		return res;
	}

	public static String doubleArrayToPrintString(double[][] m) {
		return doubleArrayToPrintString(m, ", ", Integer.MAX_VALUE, m.length, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, int toprowlim, int btmrowlim) {
		return doubleArrayToPrintString(m, ", ", toprowlim, btmrowlim, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter) {
		return doubleArrayToPrintString(m, colDelimiter, Integer.MAX_VALUE, -1, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, -1, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, btmrowlim, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim,
			int collim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, btmrowlim, collim, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim,
			int collim, String sentenceDelimiter) {
		StringBuffer str = new StringBuffer(m.length * m[0].length);

		str.append("Dim:" + m.length + " x " + m[0].length + "\n");

		int i = 0;
		for (; i < m.length && i < toprowlim; i++) {
			final String rowPref = i < 1000 ? String.format("%03d", i) : String.format("%04d", i);
			str.append(rowPref + ": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				str = str.append(String.format("%.4f", m[i][j]));
				str = str.append(colDelimiter);
			}
			str = str.append(String.format("%.4f", m[i][m[i].length - 1]));

			str.append("]");
			if (i < m.length - 1) {
				str = str.append(sentenceDelimiter);
			}
		}
		if (btmrowlim < 0)
			return str.toString();
		while (i < (m.length - btmrowlim))
			i++;
		str.append("\t.\n\t.\n\t.\n");
		for (; i < m.length; i++) {
			final String rowPref = i < 1000 ? String.format("%03d", i) : String.format("%04d", i);
			str.append(rowPref + ": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				str = str.append(String.format("%.4f", m[i][j]));
				str = str.append(colDelimiter);
			}
			str = str.append(String.format("%.4f", m[i][m[i].length - 1]));

			str.append("]");
			if (i < m.length - 1) {
				str = str.append(sentenceDelimiter);
			}
		}
		return str.toString();
	}

	public static String doubleArrayToString(double[][] m) {
		return doubleArrayToString(m, ",");
	}

	public static String doubleArrayToString(double[][] m, String colDelimiter) {
		StringBuffer str = new StringBuffer(m.length * m[0].length);
		for (final double[] element : m) {
			for (int j = 0; j < element.length - 1; j++) {
				str = str.append(Double.toString(element[j]));
				str = str.append(colDelimiter);
			}
			str = str.append(Double.toString(element[element.length - 1]));
			str = str.append("\n");
		}
		return str.toString();
	}

	// Unit Tested
	/**
	 * Returns a new matrix with values that are the log of the input matrix
	 * 
	 * @param matrix
	 * @return same matrix with values log'ed
	 */
	public static double[][] log(double[][] m1) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
			}
		}
		return matrix;
	}

	/**
	 * Returns a new matrix with values that are the log of the input matrix
	 * 
	 * @param matrix
	 * @param infAsZero
	 *            treat +- Infinity as zero, i.e replaces Infinity with 0.0 if
	 *            set to true
	 * @return same matrix with values log'ed
	 */
	public static double[][] log(double[][] m1, boolean infAsZero) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
				if (infAsZero && Double.isInfinite(matrix[i][j]))
					matrix[i][j] = 0.0;
			}
		}
		return matrix;
	}

	public static double mean(double[][] matrix) {
		return mean(matrix, 2)[0][0];
	}

	// Unit Tested
	public static double[][] mean(double[][] matrix, int axis) {
		// Axis = 0 => sum columns
		// Axis = 1 => sum rows
		// Axis = 2 => global (returns a 1 element array with the result)
		double[][] result;
		if (axis == 0) {
			result = new double[1][matrix[0].length];
			for (int j = 0; j < matrix[0].length; j++) {
				double colsum = 0.0;
				for (final double[] element : matrix) {
					colsum += element[j];
				}
				result[0][j] = colsum / matrix.length;
			}
		} else if (axis == 1) {
			result = new double[matrix.length][1];
			for (int i = 0; i < matrix.length; i++) {
				double rowsum = 0.0;
				for (int j = 0; j < matrix[0].length; j++) {
					rowsum += matrix[i][j];
				}
				result[i][0] = rowsum / matrix[0].length;
			}
		} else if (axis == 2) {
			result = new double[1][1];
			for (int j = 0; j < matrix[0].length; j++) {
				for (final double[] element : matrix) {
					result[0][0] += element[j];
				}
			}
			result[0][0] /= (matrix[0].length * matrix.length);
		} else {
			throw new IllegalArgumentException("Axes other than 0,1,2 is unsupported");
		}
		return result;
	}

	/**
	 * Replaces Infinity's with repl
	 * 
	 * @param matrix
	 * @param repl
	 * @return
	 */
	public static double[][] replaceInf(double[][] matrix, double repl) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (Double.isInfinite(matrix[i][j])) {
					result[i][j] = repl;
				} else {
					result[i][j] = matrix[i][j];
				}
			}
		}
		return result;
	}

	/**
	 * Replaces NaN's with repl
	 * 
	 * @param matrix
	 * @param repl
	 * @return
	 */
	public static double[][] replaceNaN(double[][] matrix, double repl) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (Double.isNaN(matrix[i][j])) {
					result[i][j] = repl;
				} else {
					result[i][j] = matrix[i][j];
				}
			}
		}
		return result;
	}

	public static double stdev(double[][] matrix) {
		final double m = mean(matrix);

		double total = 0;

		final int N = matrix.length * matrix[0].length;

		for (final double[] element : matrix) {
			for (int j = 0; j < element.length; j++) {
				final double x = element[j];
				total += (x - m) * (x - m);
			}
		}

		return Math.sqrt(total / (N - 1));
	}

	Random rnd = new Random();

	MatrixOp multiplyop = new MatrixOp() {
		@Override
		public double compute(double f1, double f2) {
			return f1 * f2;
		}
	};

	MatrixOp minusop = new MatrixOp() {
		@Override
		public double compute(double f1, double f2) {
			return f1 - f2;
		}
	};

	/**
	 * @param booleans
	 * @return
	 */
	double[][] abs(boolean[][] booleans) {
		final double[][] absolutes = new double[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				absolutes[i][j] = booleans[i][j] ? 1 : 0;
			}
		}
		return absolutes;
	}

	double[][] addColumnVector(double[][] matrix, double[][] colvector) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + colvector[i][0];
			}
		}
		return result;
	}

	double[][] addRowVector(double[][] matrix, double[][] rowvector) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + rowvector[0][j];
			}
		}
		return result;
	}

	// Unit Tested
	/**
	 * All values in matrix that is less than <code>lessthan</code> is assigned
	 * the value <code>assign</code>
	 * 
	 * @param matrix
	 * @param lessthan
	 * @param assign
	 * @return
	 */
	void assignAllLessThan(double[][] matrix, double lessthan, double assign) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] < lessthan) {
					matrix[i][j] = assign;
				}
			}
		}
	}

	void assignAtIndex(double[][] num, int[] range, int[] range1, double value) {
		for (int j = 0; j < range.length; j++) {
			num[range[j]][range1[j]] = value;
		}
	}

	void assignValuesToRow(double[][] matrix, int row, int[] indicies, double[] values) {
		if (indicies.length != values.length) {
			throw new IllegalArgumentException("Length of indicies and values have to be equal");
		}
		for (int j = 0; j < indicies.length; j++) {
			matrix[row][indicies[j]] = values[j];
		}
	}

	/**
	 * Returns a new matrix of booleans where true is set if the value in the
	 * matrix is bigger than value
	 * 
	 * @param matrix
	 * @param value
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	boolean[][] biggerThan(double[][] matrix, double value) {
		final boolean[][] equals = new boolean[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				equals[i][j] = Double.compare(matrix[i][j], value) == 1;
			}
		}
		return equals;
	}

	/**
	 * @param matrix
	 * @return a new vector with the column means of matrix
	 */
	double[] colMeans(double[][] matrix) {
		final int rows = matrix.length;
		final int cols = matrix[0].length;
		final double[] mean = new double[cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				mean[j] += matrix[i][j];
		for (int j = 0; j < cols; j++)
			mean[j] /= rows;
		return mean;
	}

	double[] colStddev(double[][] v) {
		final double[] var = variance(v);
		for (int i = 0; i < var.length; i++)
			var[i] = Math.sqrt(var[i]);
		return var;
	}

	// Unit Tested
	int[] concatenate(int[] v1, int[] v2) {
		final int[] result = new int[v1.length + v2.length];
		int index = 0;
		for (int i = 0; i < v1.length; i++, index++) {
			result[index] = v1[index];
		}
		for (int i = 0; i < v2.length; i++, index++) {
			result[index] = v2[i];
		}
		return result;
	}

	double[][] copyCols(double[][] input, int... indices) {
		final double[][] matrix = new double[indices.length][input.length];
		for (int i = 0; i < indices.length; i++)
			for (int j = 0; j < input.length; j++) {
				matrix[i][j] = input[j][indices[i]];
			}
		return matrix;
	}

	double[][] copyRows(double[][] input, int... indices) {
		final double[][] matrix = new double[indices.length][input[0].length];
		for (int i = 0; i < indices.length; i++)
			System.arraycopy(input[indices[i]], 0, matrix[i], 0, input[indices[i]].length);
		return matrix;
	}

	public double[][] diag(double[][] ds) {
		final boolean isLong = ds.length > ds[0].length;
		final int dim = Math.max(ds.length, ds[0].length);
		final double[][] result = new double[dim][dim];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {
				if (i == j) {
					if (isLong)
						result[i][j] = ds[i][0];
					else
						result[i][j] = ds[0][i];
				}
			}
		}

		return result;
	}

	/**
	 * Generate random draw from Normal with mean mu and std. dev sigma
	 * 
	 * @param mu
	 * @param sigma
	 * @return random sample
	 */
	double dnrom(double mu, double sigma) {
		return mu + (ThreadLocalRandom.current().nextGaussian() * sigma);
	}

	/**
	 * Returns a new matrix of booleans where true is set if the values to the
	 * two matrices are the same at that index
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	boolean[][] equal(boolean[][] matrix1, boolean[][] matrix2) {
		final boolean[][] equals = new boolean[matrix1.length][matrix1[0].length];
		if (matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if (matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = (matrix1[i][j] == matrix2[i][j]);
			}
		}
		return equals;
	}

	// Unit Tested
	/**
	 * Returns a new matrix of booleans where true is set if the values to the
	 * two matrices are the same at that index
	 * 
	 * @param matrix1
	 * @param matrix2
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	boolean[][] equal(double[][] matrix1, double[][] matrix2) {
		final boolean[][] equals = new boolean[matrix1.length][matrix1[0].length];
		if (matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if (matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = Double.compare(matrix1[i][j], matrix2[i][j]) == 0;
			}
		}
		return equals;
	}

	// Unit Tested
	/**
	 * Destructively sets the values in matrix to its exponentiated value
	 * 
	 * @param matrix
	 * @return same matrix with values exponentiated
	 */
	double[][] exp(double[][] m1) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.exp(m1[i][j]);
			}
		}
		return matrix;
	}

	double[][] fillMatrix(int rows, int cols, double fillvalue) {
		final double[][] matrix = new double[rows][cols];
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				matrix[i][j] = fillvalue;
		return matrix;
	}

	double[][] fillWithRow(double[][] matrix, int row) {
		final int rows = matrix.length;
		final int cols = matrix[0].length;
		final double[][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			System.arraycopy(matrix[row], 0, result[i], 0, cols);
		}
		return result;
	}

	double[][] fillWithRowOld(double[][] matrix, int row) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[row][j];
			}
		}
		return result;
	}

	double[][] getValuesFromRow(double[][] matrix, int row, int[] indicies) {
		final double[][] values = new double[1][indicies.length];
		for (int j = 0; j < indicies.length; j++) {
			values[0][j] = matrix[row][indicies[j]];
		}
		return values;
	}

	/**
	 * Return a new matrix with the max value of either the value in the matrix
	 * or maxval otherwise
	 * 
	 * @param matrix
	 * @param maxval
	 * @return
	 */
	double[][] maximum(double[][] matrix, double maxval) {
		final double[][] maxed = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				maxed[i][j] = matrix[i][j] > maxval ? matrix[i][j] : maxval;
			}
		}
		return maxed;
	}

	// Unit Tested
	/**
	 * @param vector
	 * @return mean of values in vector
	 */
	double mean(double[] vector) {
		double sum = 0.0;
		for (final double element : vector) {
			sum += element;
		}
		return sum / vector.length;
	}

	double[][] minus(double[][] m1, double[][] m2) {
		return parScalarMinus(m1, m2);
	}

	/**
	 * Returns a new matrix which is the transpose of input matrix
	 * 
	 * @param matrix
	 * @return
	 */
	double[][] naivetranspose(double[][] matrix) {
		final int cols = matrix[0].length;
		final int rows = matrix.length;
		final double[][] transpose = new double[cols][rows];
		for (int i = 0; i < cols; i++)
			for (int j = 0; j < rows; j++)
				transpose[i][j] = matrix[j][i];
		return transpose;
	}

	/**
	 * @param booleans
	 * @return new matrix with booleans which are the negations of the input
	 */
	boolean[][] negate(boolean[][] booleans) {
		final boolean[][] negates = new boolean[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				negates[i][j] = !booleans[i][j];
			}
		}
		return negates;
	}

	double[][] normalize(double[][] x, double[] meanX, double[] stdevX) {
		final double[][] y = new double[x.length][x[0].length];
		for (int i = 0; i < y.length; i++)
			for (int j = 0; j < y[i].length; j++)
				y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
		return y;
	}

	double[][] parScalarMinus(double[][] m1, double[][] m2) {
		final int ll = 600;
		final double[][] result = new double[m1.length][m1[0].length];

		final MatrixOperator process = new MatrixOperator(m1, m2, result, minusop, 0, m1.length, ll);
		pool.invoke(process);
		return result;
	}

	double[][] parScalarMultiply(double[][] m1, double[][] m2) {
		final int ll = 600;
		final double[][] result = new double[m1.length][m1[0].length];

		final MatrixOperator process = new MatrixOperator(m1, m2, result, multiplyop, 0, m1.length, ll);
		pool.invoke(process);
		return result;
	}

	void plainTranspose(double[][] matrix, double[][] transpose, int startRow, int endRow) {
		final int cols = matrix[0].length;
		for (int i = 0; i < cols; i++)
			for (int j = startRow; j < endRow; j++)
				transpose[i][j] = matrix[j][i];
	}

	double[][] plus(double[][] m1, double[][] m2) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2[i][j];
		return matrix;
	}

	int[] range(int n) {
		final int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = i;
		}
		return result;
	}

	int[] range(int a, int b) {
		if (b < a) {
			throw new IllegalArgumentException("b has to be larger than a");
		}
		int val = a;
		final int[] result = new int[b - a];
		for (int i = 0; i < (b - a); i++) {
			result[i] = val++;
		}
		return result;
	}

	/**
	 * @param m
	 * @param n
	 * @return new 2D matrix with normal random values with mean 0 and std. dev
	 *         1
	 */
	double[][] rnorm(int m, int n) {
		final double[][] array = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = dnrom(0.0, 1.0);
			}
		}
		return array;
	}

	// Unit Tested
	double[][] scalarDivide(double[][] numerator, double denom) {
		final double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom;
		return matrix;
	}

	// Unit Tested
	double[][] scalarDivide(double[][] numerator, double[][] denom) {
		final double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom[i][j];
		return matrix;
	}

	// Unit Tested
	/**
	 * @param vector
	 * @return scalar inverse of vector
	 */
	double[] scalarInverse(double[] v1) {
		final double[] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = 1 / v1[i];
		}
		return vector;
	}

	// Unit Tested
	/**
	 * @param matrix
	 * @return scalar inverse of matrix
	 */
	double[][] scalarInverse(double[][] m1) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = 1 / m1[i][j];
			}
		}
		return matrix;
	}

	// Unit Tested
	double[][] scalarMult(double[][] m1, double mul) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[i].length; j++)
				matrix[i][j] = m1[i][j] * mul;
		return matrix;
	}

	double[][] scalarMultiply(double[][] m1, double[][] m2) {
		return parScalarMultiply(m1, m2);
	}

	// Unit Tested
	double[][] scalarPlus(double[][] m1, double m2) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2;
		return matrix;
	}

	double[][] scalarPow(double[][] matrix, double power) {
		final double[][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] += Math.pow(matrix[i][j], power);
			}
		}
		return result;
	}

	double[][] sign(double[][] matrix) {
		final double[][] signs = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				signs[i][j] = matrix[i][j] >= 0 ? 1 : -1;
			}
		}
		return signs;
	}

	// Unit Tested
	double[][] sMinus(double[][] m1, double[][] m2) {
		final double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] - m2[i][j];
		return matrix;
	}

	// Unit Tested
	double[][] sMultiply(double[][] v1, double[][] v2) {
		if (v1.length != v2.length || v1[0].length != v2[0].length) {
			throw new IllegalArgumentException("a and b has to be of equal dimensions");
		}
		final double[][] result = new double[v1.length][v1[0].length];
		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v1[0].length; j++) {
				result[i][j] = v1[i][j] * v2[i][j];
			}
		}
		return result;
	}

	// Unit Tested
	/**
	 * Destructively sets the values in vector to its square root
	 * 
	 * @param vector
	 * @return same vector with values sqrt'ed
	 */
	double[] sqrt(double[] v1) {
		final double[] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.sqrt(v1[i]);
		}
		return vector;
	}

	// Unit Tested
	/**
	 * @param matrix
	 * @return a new matrix with the values of matrix squared
	 */
	double[][] square(double[][] matrix) {
		return scalarPow(matrix, 2);
	}

	// Unit Tested
	/**
	 * @param matrix
	 * @return sum of all values in the matrix
	 */
	double sum(double[][] matrix) {
		double sum = 0.0;
		for (final double[] element : matrix) {
			for (int j = 0; j < matrix[0].length; j++) {
				sum += element[j];
			}
		}
		return sum;
	}

	// Unit Tested
	// Should be called dim-sum! :)
	double[][] sum(double[][] matrix, int axis) {
		// Axis = 0 => sum columns
		// Axis = 1 => sum rows
		double[][] result;
		if (axis == 0) {
			result = new double[1][matrix[0].length];
			for (int j = 0; j < matrix[0].length; j++) {
				double rowsum = 0.0;
				for (final double[] element : matrix) {
					rowsum += element[j];
				}
				result[0][j] = rowsum;
			}
		} else if (axis == 1) {
			result = new double[matrix.length][1];
			for (int i = 0; i < matrix.length; i++) {
				double colsum = 0.0;
				for (int j = 0; j < matrix[0].length; j++) {
					colsum += matrix[i][j];
				}
				result[i][0] = colsum;
			}
		} else {
			throw new IllegalArgumentException("Axes other than 0,1 is unsupported");
		}
		return result;
	}

	// Unit Tested
	double[][] tile(double[][] matrix, int rowtimes, int coltimes) {
		final double[][] result = new double[matrix.length * rowtimes][matrix[0].length * coltimes];
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (final double[] element : matrix) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix[0].length; l++) {
						result[resultrow][resultcol++] = element[l];
					}
				}
				resultrow++;
			}
		}

		return result;
	}

	// Unit Tested
	double[][] times(double[][] m1, double[][] m2) {
		final Matrix A = Matrix.constructWithCopy(m1);
		final Matrix B = Matrix.constructWithCopy(m2);
		return A.times(B).getArray();
	}

	// Unit Tested
	double[][] transpose(double[][] matrix) {
		return transpose(matrix, 1000);
	}

	// Unit Tested
	/**
	 * Returns a new matrix which is the transpose of input matrix
	 * 
	 * @param matrix
	 * @return
	 */
	double[][] transpose(double[][] matrix, int ll) {
		final int cols = matrix[0].length;
		final int rows = matrix.length;
		final double[][] transpose = new double[cols][rows];
		if (rows < 100) {
			for (int i = 0; i < cols; i++)
				for (int j = 0; j < rows; j++)
					transpose[i][j] = matrix[j][i];
		} else {
			final MatrixTransposer process = new MatrixTransposer(matrix, transpose, 0, rows, ll);
			pool.invoke(process);
		}
		return transpose;
	}

	double[] variance(double[][] v) {
		final int m = v.length;
		final int n = v[0].length;
		final double[] var = new double[n];
		final int degrees = (m - 1);
		double c;
		double s;
		for (int j = 0; j < n; j++) {
			c = 0;
			s = 0;
			for (int k = 0; k < m; k++)
				s += v[k][j];
			s = s / m;
			for (int k = 0; k < m; k++)
				c += (v[k][j] - s) * (v[k][j] - s);
			var[j] = c / degrees;
		}
		return var;
	}

}
