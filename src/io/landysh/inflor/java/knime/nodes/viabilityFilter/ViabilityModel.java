package io.landysh.inflor.java.knime.nodes.viabilityFilter;

import io.landysh.inflor.java.core.FCSVector.FCSVector;
import io.landysh.inflor.java.core.utils.HistogramCalculator;

public class ViabilityModel {
	
	
	
	public ViabilityModel(String[] columnNames) {
		// TODO Auto-generated constructor stub
	}

	public void buildModel(FCSVector viabilityData) {
		HistogramCalculator calc = new HistogramCalculator(viabilityData.getData(), false);
		calc.createBins(viabilityData.getDisplayRangeMin(), viabilityData.getDisplayRangeMax(), 512);
		calc.findPeaks();
	}

	public boolean[] scoreModel(FCSVector viabilityData) {
		// TODO Auto-generated method stub
		return null;
	}
}
