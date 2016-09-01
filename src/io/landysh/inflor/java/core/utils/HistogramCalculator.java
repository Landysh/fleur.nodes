package io.landysh.inflor.java.core.utils;

import java.util.ArrayList;

public class HistogramCalculator {

	public static double[] smoothCounts(int[] counts, int windowSize) {
		// TODO: MVP: only simple trailing for now. should likely use center
		// weighted.
		final double[] smoothedCounts = new double[counts.length];
		for (int i = 0; i < counts.length; i++) {
			if (i <= windowSize) {
				smoothedCounts[i] = counts[i];
			} else {
				int sum = 0;
				for (int j = 0; j < windowSize; j++) {
					sum = sum + counts[j];
				}
				final double smoothed = sum / windowSize;
				smoothedCounts[i] = smoothed;
			}
		}
		return smoothedCounts;
	}

	private final double[] data;
	private double[] bins;

	private int[] counts;
	private ArrayList<Double> peakList = null;

	private ArrayList<Double> valleyList = null;

	public HistogramCalculator(double[] data, boolean autoBin) {
		// TODO: #MVP #PERFORMANCE sorting the data may be helpful later
		this.data = data;
		if (autoBin == true) {
			bins = autoBin(data);
			counts = binHistogram(this.data, bins);
		}
	}

	private double[] autoBin(double[] data) {
		final int binCount = (int) Math.sqrt(data.length);
		final double[] bins = createBins(data, binCount);
		return bins;
	}

	public int[] binHistogram(double[] data, double[] bins) {
		final int[] counts = new int[bins.length];
		for (final double d : data) {
			@SuppressWarnings("unused")
			// TODO: #MVP #PERFORMANCE weird?
			boolean binned = false;
			while (binned = false) {
				for (int i = 0; i < bins.length; i++) {
					if (d < bins[i]) {
						counts[i]++;
						binned = true;
					}
				}
			}
		}
		this.counts = counts;
		return this.counts;
	}

	public double[] createBins(double min, double max, int binCount) {
		final double[] bins = new double[binCount];
		final double delta = (max - min) / (binCount);
		for (int i = 0; i <= binCount; i++) {
			bins[i] = min;
			min = min + delta;
		}
		this.bins = bins;
		return this.bins;
	}

	private double[] createBins(double[] data, int binCount) {
		final VectorCalculator calc = new VectorCalculator(data);
		final double min = calc.getMin();
		final double max = calc.getMax();
		final double[] bins = this.createBins(min, max, binCount);
		return bins;
	}

	public Double[] findPeaks() {
		if (peakList == null) {
			peakFind(smoothCounts(counts, bins.length / 20), bins);
		}
		return peakList.toArray(new Double[peakList.size()]);
	}

	public Double[] findValleys() {
		if (peakList == null) {
			peakFind(smoothCounts(counts, bins.length / 20), bins);
		}
		return valleyList.toArray(new Double[valleyList.size()]);
	}

	private void peakFind(double[] counts, double[] bins) {
		final ArrayList<Double> peaks = new ArrayList<Double>();
		final ArrayList<Double> valleys = new ArrayList<Double>();

		final Double lagFD = counts[1] - counts[0];
		final Double lagVal = counts[1];
		for (int i = 2; i < counts.length; i++) {
			final Double fd = counts[i] - lagVal;
			if (fd <= 0 && lagFD >= 0) {
				peaks.add(bins[i]);
			} else if (fd >= 0 && lagFD <= 0) {
				valleys.add(bins[i]);
			}
		}
		peakList = peaks;
		valleyList = valleys;
	}

}