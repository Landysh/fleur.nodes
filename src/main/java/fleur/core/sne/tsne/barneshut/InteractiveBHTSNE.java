package fleur.core.sne.tsne.barneshut;


public interface InteractiveBHTSNE extends BarnesHutTSne {  
  public double[][] runInteractively();

  void init(double[][] inX, int N, int D, int dimCount, int initDimCount, double perplexity,
      int maxIterations, boolean usePCA, double theta, int seed);
  
  @Override
  public double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity,
      int max_iter, boolean use_pca, double theta) throws InterruptedException;
  
}
