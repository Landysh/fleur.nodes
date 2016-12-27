package main.java.inflor.core.sne.tsne.barneshut;

import org.knime.core.node.CanceledExecutionException;

import main.java.inflor.core.sne.tsne.*;

public interface BarnesHutTSne extends TSne {
  public double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity,
      int max_iter, boolean use_pca, double theta) throws CanceledExecutionException ;
}
