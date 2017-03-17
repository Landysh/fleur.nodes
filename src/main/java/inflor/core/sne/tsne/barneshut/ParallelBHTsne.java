package inflor.core.sne.tsne.barneshut;

import static java.lang.Math.exp;
import static java.lang.Math.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

import org.knime.core.node.CanceledExecutionException;

import inflor.core.sne.utils.*;

public class ParallelBHTsne extends BHTSne {

  private ForkJoinPool gradientPool;
  private ExecutorService gradientCalculationPool;

  @SuppressWarnings("serial")
  class RecursiveGradientCalculator extends RecursiveAction {
    int startRow = -1;
    int endRow = -1;
    int limit = 100;
    transient SPTree tree;
    double[][] negF;
    double theta;
    transient AtomicDouble sumQ;

    public RecursiveGradientCalculator(SPTree tree, double[][] negF, double theta,
        AtomicDouble sumQ, int startRow, int endRow, int ll) {
      this.limit = ll;
      this.startRow = startRow;
      this.endRow = endRow;
      this.tree = tree;
      this.negF = negF;
      this.theta = theta;
      this.sumQ = sumQ;
    }

    @Override
    protected void compute() {
      if ((endRow - startRow) <= limit) {
        for (int row = startRow; row < endRow; row++) {
          tree.computeNonEdgeForces(row, theta, negF[row], sumQ);
        }
      } else {
        int range = endRow - startRow;
        int startDoc1 = startRow;
        int endDoc1 = startRow + (range / 2);
        int startDoc2 = endDoc1;
        int endDoc2 = endRow;
        invokeAll(
            new RecursiveGradientCalculator(tree, negF, theta, sumQ, startDoc1, endDoc1, limit),
            new RecursiveGradientCalculator(tree, negF, theta, sumQ, startDoc2, endDoc2, limit));
      }
    }
  }

  class ParallelGradientCalculator implements Callable<Double> {
    int row = -1;
    int limit = 100;
    ParallelSPTree tree;
    double[][] negF;
    double theta;

    public ParallelGradientCalculator(ParallelSPTree tree, double[][] negF, double theta, int row,
        int ll) {
      this.limit = ll;
      this.row = row;
      this.tree = tree;
      this.negF = negF;
      this.theta = theta;
    }

    @Override
    public Double call() {
      return tree.computeNonEdgeForces(row, theta, negF[row], 0.0);
    }
  }

  @Override
  double[][] run(double[][] x, int n, int d, int dimCount, int initDimCount, double perplexity,
      int maxIter, boolean usePCA, double theta) throws CanceledExecutionException {
    gradientPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    gradientCalculationPool =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    double[][] y = super.run(x, n, d, dimCount, initDimCount, perplexity, maxIter, usePCA, theta);
    gradientPool.shutdown();
    gradientCalculationPool.shutdown();
    return y;
  }

  @SuppressWarnings("serial")
  class RecursiveGradientUpdater extends RecursiveAction {
    int startIdx = -1;
    int endIdx = -1;
    int limit = 100;
    int n;
    int dimCount;
    double[] y;
    double momentum;
    double eta;
    double[] dY;
    double[] uY;
    double[] gains;

    public RecursiveGradientUpdater(int n, int dimCount, double[] y, double momentum, double eta,
        double[] dY, double[] uY, double[] gains, int startIdx, int endIdx, int limit) {
      super();
      this.startIdx = startIdx;
      this.endIdx = endIdx;
      this.limit = limit;
      this.n = n;
      this.dimCount = dimCount;
      this.y = y;
      this.momentum = momentum;
      this.eta = eta;
      this.dY = dY;
      this.uY = uY;
      this.gains = gains;
    }

    @Override
    protected void compute() {
      if ((endIdx - startIdx) <= limit) {
        for (int n2 = startIdx; n2 < endIdx; n2++) {
          // Update gains
          gains[n2] = (sign_tsne(dY[n2]) != sign_tsne(uY[n2])) ? (gains[n2] + .2) : (gains[n2] * .8);
          if (gains[n2] < .01)
            gains[n2] = .01;

          // Perform gradient update (with momentum and gains)
          y[n2] = y[n2] + uY[n2];
          uY[n2] = momentum * uY[n2] - eta * gains[n2] * dY[n2];
        }
      } else {
        int range = (endIdx - startIdx);
        int startIdx1 = startIdx;
        int endIdx1 = startIdx + (range / 2);
        int endIdx2 = endIdx;
        invokeAll(
            new RecursiveGradientUpdater(n, dimCount, y, momentum, eta, dY, uY, gains, startIdx1,
                endIdx1, limit),
            new RecursiveGradientUpdater(n, dimCount, y, momentum, eta, dY, uY, gains, endIdx1,
                endIdx2, limit));
      }
    }
  }

  @Override
  void updateGradient(int n, int dimCount, double[] y, double momentum, double eta, double[] dY,
      double[] uY, double[] gains) {
    RecursiveGradientUpdater dslr = new RecursiveGradientUpdater(n, dimCount, y, momentum, eta, dY,
        uY, gains, 0, n * dimCount, n / (Runtime.getRuntime().availableProcessors() * 10));
    gradientPool.invoke(dslr);
  }

  // Compute gradient of the t-SNE cost function (using Barnes-Hut algorithm)
  @Override
  void computeGradient(double[] p, int[] inRowP, int[] inColP, double[] inValP, double[] y,
      int N, int d, double[] dC, double theta) {
    // Construct space-partitioning tree on current map
    ParallelSPTree tree = new ParallelSPTree(d, y, N);

    // Compute all terms required for t-SNE gradient
    double[] posF = new double[N * d];
    double[][] negF = new double[N][d];

    tree.computeEdgeForces(inRowP, inColP, inValP, N, posF);

    double sumQ = 0;

    List<ParallelGradientCalculator> calculators = new ArrayList<>();
    for (int n = 0; n < N; n++) {
      calculators.add(new ParallelGradientCalculator(tree, negF, theta, n, 20));
    }
    List<Future<Double>> results;
    try {
      results = gradientCalculationPool.invokeAll(calculators);
      for (Future<Double> result : results) {
        double tmp = result.get();
        sumQ += tmp;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(-1);
    } catch (ExecutionException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    // Compute final t-SNE gradient
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < d; j++) {
        dC[i * d + j] = posF[i * d + j] - (negF[i][j] / sumQ);
      }
    }
  }



  @Override
  // Compute input similarities with a fixed perplexity using ball trees
  void computeGaussianPerplexity(double[] x, int N, int d, int[] inRowP, int[] inColP,
      double[] inValP, double perplexity, int k) {
    if (perplexity > k)
      System.out.println("Perplexity should be lower than K!");

    // Allocate the memory we need
    int[] rowP = inRowP;
    int[] colP = inColP;
    double[] valP = inValP;
    double[] curP = new double[N - 1];

    rowP[0] = 0;
    for (int n = 0; n < N; n++)
      rowP[n + 1] = rowP[n] + k;

    List<Future<ParallelVpTree<DataPoint>.ParallelTreeNode.TreeSearchResult>> results =
        createFutureTreeResults(x, N, d, k);

    for (Future<ParallelVpTree<DataPoint>.ParallelTreeNode.TreeSearchResult> result : results) {
      ParallelVpTree<DataPoint>.ParallelTreeNode.TreeSearchResult res = null;
      List<Double> distances = null;
      List<DataPoint> indices = null;
      int n = -1;
      try {
        res = result.get();
        distances = res.getDistances();
        indices = res.getIndices();
        n = res.getIndex();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }

      // Initialize some variables for binary search
      boolean found = false;
      double beta = 1.0;
      double minBeta = -Double.MAX_VALUE;
      double maxBeta = Double.MAX_VALUE;
      double tol = 1e-5;

      // Iterate until we found a good perplexity
      int iter = 0;
      double sumP = 0.;
      while (!found && iter < 200) {

        // Compute Gaussian kernel row and entropy of current row
        sumP = Double.MIN_VALUE;
        double h = .0;
        for (int m = 0; m < k; m++) {
          curP[m] = exp(-beta * distances.get(m + 1));
          sumP += curP[m];
          h += beta * (distances.get(m + 1) * curP[m]);
        }
        h = (h / sumP) + log(sumP);

        // Evaluate whether the entropy is within the tolerance level
        double hDiff = h - log(perplexity);
        if (hDiff < tol && -hDiff < tol) {
          found = true;
        } else {
          if (hDiff > 0) {
            minBeta = beta;
            if (maxBeta == Double.MAX_VALUE || maxBeta == -Double.MAX_VALUE)
              beta *= 2.0;
            else
              beta = (beta + maxBeta) / 2.0;
          } else {
            maxBeta = beta;
            if (minBeta == -Double.MAX_VALUE || minBeta == Double.MAX_VALUE)
              beta /= 2.0;
            else
              beta = (beta + minBeta) / 2.0;
          }
        }

        // Update iteration counter
        iter++;
      }

      // Row-normalize current row of P and store in matrix
      for (int m = 0; m < k; m++) {
        curP[m] /= sumP;
        colP[rowP[n] + m] = indices.get(m + 1).index();
        valP[rowP[n] + m] = curP[m];
      }
    }
  }

  private List<Future<ParallelVpTree<DataPoint>.ParallelTreeNode.TreeSearchResult>> createFutureTreeResults(
      double[] x, int N, int d, int k) {
    // Build ball tree on data set
    ParallelVpTree<DataPoint> tree = new ParallelVpTree<>(gradientPool, distance);
    final DataPoint[] objX = new DataPoint[N];
    for (int n = 0; n < N; n++) {
      double[] row = MatrixOps.extractRowFromFlatMatrix(x, n, d);
      objX[n] = new DataPoint(d, n, row);
    }
    tree.create(objX);

    // Loop over all points to find nearest neighbors
    List<Future<ParallelVpTree<DataPoint>.ParallelTreeNode.TreeSearchResult>> results =
        tree.searchMultiple(tree, objX, k + 1);
    return results;
  }

}
