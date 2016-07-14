package io.landysh.inflor.java.core.gatingML.gates.polygonGate;

import java.util.ArrayList;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGMLDimension;

public class PolygonDimension extends AbstractGMLDimension {

	ArrayList<Double> points;

	public PolygonDimension(String name) {
		super(name);
		points = new ArrayList<Double>();
	}

	public ArrayList<Double> getPoints() {
		return points;
	}

	public void addPoint(Double d) {
		points.add(d);
	}

	public void updatePoint(int index, Double newValue) {
		if (index < points.size()) {
			points.set(index, newValue);
		} else {
			String message = "Attempting to update a nonexistent point!";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}
}
