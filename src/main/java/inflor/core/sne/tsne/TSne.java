package inflor.core.sne.tsne;

import org.knime.core.node.CanceledExecutionException;

/**
*
* Author: Leif Jonsson (leif.jonsson@gmail.com)
* 
* This is a Java implementation of van der Maaten and Hintons t-sne 
* dimensionality reduction technique that is particularly well suited 
* for the visualization of high-dimensional datasets
*
*/
public interface TSne {

	double [][] tsne(double[][] X, int k, int initial_dims, double perplexity) throws CanceledExecutionException;
	double [][] tsne(double[][] X, int k, int initial_dims, double perplexity, int maxIterations) throws CanceledExecutionException;

	double [][] tsne(double[][] X, int no_dims, int initial_dims, double perplexity, int max_iter, boolean use_pca) throws CanceledExecutionException;

	static class R {
		double [][] P;
		double [] beta;
		double H;
	}
}
