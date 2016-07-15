package io.landysh.inflor.java.core.utils;

import java.util.ArrayList;

public class HistogramCalculator {

	private double[] data;
	private double[] bins;
	private int[] counts;

	private ArrayList<Double> peakList = null;
	private ArrayList<Double> valleyList = null;

	public HistogramCalculator(double[] data, boolean autoBin) {
		// TODO: #MVP #PERFORMANCE sorting the data may be helpful later
		this.data = data;
		if (autoBin == true) {
			this.bins = autoBin(data);
			this.counts = binHistogram(this.data, this.bins);
		}
	}

	public double[] createBins(double min, double max, int binCount) {
		double[] bins = new double[binCount];
		double delta = (max - min) / (binCount);
		for (int i = 0; i <= binCount; i++) {
			bins[i] = min;
			min = min + delta;
		}
		this.bins = bins;
		return this.bins;
	}

	private double[] createBins(double[] data, int binCount) {
		VectorCalculator calc = new VectorCalculator(data);
		double min = calc.getMin();
		double max = calc.getMax();
		double[] bins = this.createBins(min, max, binCount);
		return bins;
	}

	private double[] autoBin(double[] data) {
		int binCount = (int) Math.sqrt(data.length);
		double[] bins = createBins(data, binCount);
		return bins;
	}

	public int[] binHistogram(double[] data, double[] bins) {
		int[] counts = new int[bins.length];
		for (double d : data) {
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

	public static double[] smoothCounts(int[] counts, int windowSize) {
		// TODO: MVP: only simple trailing for now. should likely use center
		// weighted.
		double[] smoothedCounts = new double[counts.length];
		for (int i = 0; i < counts.length; i++) {
			if (i <= windowSize) {
				smoothedCounts[i] = counts[i];
			} else {
				int sum = 0;
				for (int j = 0; j < windowSize; j++) {
					sum = sum + counts[j];
				}
				double smoothed = sum / windowSize;
				smoothedCounts[i] = smoothed;
			}
		}
		return smoothedCounts;
	}

	private void peakFind(double[] counts, double[] bins) {
		ArrayList<Double> peaks = new ArrayList<Double>();
		ArrayList<Double> valleys = new ArrayList<Double>();

		Double lagFD = counts[1] - counts[0];
		Double lagVal = counts[1];
		for (int i = 2; i < counts.length; i++) {
			Double fd = counts[i] - lagVal;
			if (fd <= 0 && lagFD >= 0) {
				peaks.add(bins[i]);
			} else if (fd >= 0 && lagFD <= 0) {
				valleys.add(bins[i]);
			}
		}
		this.peakList = peaks;
		this.valleyList = valleys;
	}

	public Double[] findPeaks() {
		if (peakList == null) {
			peakFind(smoothCounts(this.counts, this.bins.length / 20), this.bins);
		}
		return this.peakList.toArray(new Double[this.peakList.size()]);
	}

	public Double[] findValleys() {
		if (peakList == null) {
			peakFind(smoothCounts(this.counts, this.bins.length / 20), this.bins);
		}
		return this.valleyList.toArray(new Double[this.valleyList.size()]);
	}

}