package io.landysh.inflor.knime.nodes.summaryStats;

public enum SummaryStatTypes {
	MEDIAN("Median"), MEAN("Mean"), GEOMEAN("Geomentric Mean"), VARIANCE("Variance");

	private final String label;

	SummaryStatTypes(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}
