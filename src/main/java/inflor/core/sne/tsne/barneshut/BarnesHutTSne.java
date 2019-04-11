package inflor.core.sne.tsne.barneshut;

import inflor.core.sne.tsne.*;

public interface BarnesHutTSne extends TSne {
	  double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca,
			  double theta) throws InterruptedException;
}
