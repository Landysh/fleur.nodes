package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import io.landysh.inflor.java.core.dataStructures.Histogram1D;

public class ViabilityModel {

	public ViabilityModel(String[] columnNames) {
		// TODO Auto-generated constructor stub
	}

	public void buildModel(double[] data) {
		final Histogram1D histogram = new Histogram1D(data, 512);
		histogram.findPeaks();
	}

	public boolean[] scoreModel(double[] viabilityData) {
		// TODO Auto-generated method stub
		return null;
	}
}
