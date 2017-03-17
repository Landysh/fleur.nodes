package inflor.core.sne.tsne.barneshut;

import org.knime.core.node.CanceledExecutionException;

public interface InteractiveBHTSNE extends BarnesHutTSne {  
  public double[][] runInteractively();

  void init(double[][] inX, int N, int D, int dimCount, int initDimCount, double perplexity,
      int maxIterations, boolean usePCA, double theta);
  
  @Override
  public double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity,
      int max_iter, boolean use_pca, double theta) throws CanceledExecutionException;
  
  
}
