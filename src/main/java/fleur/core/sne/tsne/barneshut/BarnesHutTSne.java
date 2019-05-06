package fleur.core.sne.tsne.barneshut;

import fleur.core.sne.tsne.*;

public interface BarnesHutTSne extends TSne {
	  double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca,
			  double theta) throws InterruptedException;
}
