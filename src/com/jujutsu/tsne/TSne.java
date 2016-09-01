package com.jujutsu.tsne;

/**
 *
 * Author: Leif Jonsson (leif.jonsson@gmail.com)
 * 
 * This is a port of van der Maaten and Hintons Python implementation of t-sne
 *
 */
public interface TSne {

	static class R {
		double[][] P;
		double[] beta;
		double H;
	}

	R Hbeta(double[][] D, double beta);

	double[][] tsne(double[][] X, int k, int initial_dims, double perplexity);

	double[][] tsne(double[][] X, int k, int initial_dims, double perplexity, int maxIterations);

	double[][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca);

	R x2p(double[][] X, double tol, double perplexity);
}
