package io.landysh.inflor.java.core.gatingML.gates.polygonGate;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Element;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGMLDimension;
import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;

public class PolygonGate extends AbstractGate {

	public class PolygonDimension extends AbstractGMLDimension {

		ArrayList<Double> points;

		public PolygonDimension(String name) {
			super(name);
			points = new ArrayList<Double>();
		}

		public void addPoint(Double d) {
			points.add(d);
		}

		public ArrayList<Double> getPoints() {
			return points;
		}

		public void removePoint(int index) {
			points.remove(index);

		}

		public void setPoints(ArrayList<Double> newPoints) {
			points = newPoints;
		}

		public void updatePoint(int index, Double newValue) {
			if (index < points.size()) {
				points.set(index, newValue);
			} else {
				final String message = "Attempting to update a nonexistent point!";
				final IllegalStateException ise = new IllegalStateException(message);
				ise.printStackTrace();
				throw ise;
			}
		}
	}

	PolygonDimension d1;
	PolygonDimension d2;

	public PolygonGate(String id) {
		super(id);
	}

	public void addPoint(double d1, double d2) {
		this.d1.addPoint(d1);
		this.d2.addPoint(d2);
	}

	@Override
	public boolean[] evaluate(ConcurrentHashMap<String, double[]> data, int rowCount) {
		validateWithData(data.keySet());
		final PolygonCalculator poly = new PolygonCalculator(d1.points, d2.points);
		final boolean[] result = new boolean[rowCount];
		final double[] d1Data = data.get(d1.getName());
		final double[] d2Data = data.get(d2.getName());
		for (int i = 0; i < d1Data.length; i++) {
			if (poly.isInside(d1Data[i], d2Data[i]) == true) {
				result[i] = true;
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	public ArrayList<String> getDimensionNames() {
		final ArrayList<String> names = new ArrayList<String>();
		names.add(d1.getName());
		names.add(d2.getName());
		return names;
	}

	public Double[] getVertex(int index) {
		final Double[] vertex = new Double[] { d1.getPoints().get(index), d2.getPoints().get(index) };
		return vertex;
	}

	public int getVertexCount() {
		if (d1.points != null && d2.points != null && d1.points.size() == d2.points.size()) {
			return d1.points.size();
		} else {
			final String message = "both dimensions must both be initialized and have the same number of points.";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	public void removePoint(int index) {
		d1.removePoint(index);
		d2.removePoint(index);
	}

	public void setDimensions(String d1, String d2) {
		this.d1 = new PolygonDimension(d1);
		this.d2 = new PolygonDimension(d2);
	}

	public void setPoints(ArrayList<Double> d1Points, ArrayList<Double> d2Points) {
		d1.setPoints(d1Points);
		d2.setPoints(d2Points);
	}

	@Override
	public Element toXMLElement() {
		// TODO
		return null;
	}

	public void updatePoint(int index, double d1New, double d2New) {
		d1.updatePoint(index, d1New);
		d2.updatePoint(index, d2New);
	}

	@Override
	public void validate() throws IllegalStateException {
		if (getVertexCount() < 3) {
			final String message = "A polygon requires at least 3 verticies!";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}

		if (d1.getPoints().size() != d2.getPoints().size()) {
			final String message = "A polygon requires the same number of points in both dimensions.";
			final IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	private void validateWithData(Set<String> keySet) {
		if (keySet.contains(d1.getName()) == true && keySet.contains(d2.getName()) == true) {
			validate();
		}
	}
}
// EOF