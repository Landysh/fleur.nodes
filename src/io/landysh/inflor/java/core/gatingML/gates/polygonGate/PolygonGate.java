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

		public void removePoint(int index) {
			this.points.remove(index);
			
		}

		public void setPoints(ArrayList<Double> newPoints) {
			this.points = newPoints;
		}
	}

	PolygonDimension d1;
	PolygonDimension d2;

	public PolygonGate(String id) {
		super(id);
	}

	@Override
	public boolean[] evaluate(ConcurrentHashMap<String, double[]> data, int rowCount) {
		validateWithData(data.keySet());
		PolygonCalculator poly = new PolygonCalculator(d1.points, d2.points);
		boolean[] result = new boolean[rowCount];
		double[] d1Data = data.get(d1.getName());
		double[] d2Data = data.get(d2.getName());
		for (int i=0; i<d1Data.length; i++) {
			if (poly.isInside(d1Data[i], d2Data[i]) == true) {
				result[i] = true;
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	private void validateWithData(Set<String> keySet) {
		if (keySet.contains(d1.getName()) == true && keySet.contains(d2.getName()) == true) {
			validate();
		}
	}

	@Override
	public Element toXMLElement() {
		// TODO
		return null;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (getVertexCount() < 3) {
			String message = "A polygon requires at least 3 verticies!";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}

		if (d1.getPoints().size() != d2.getPoints().size()) {
			String message = "A polygon requires the same number of points in both dimensions.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	public int getVertexCount() {
		if (d1.points != null && d2.points != null && d1.points.size() == d2.points.size()) {
			return d1.points.size();
		} else {
			String message = "both dimensions must both be initialized and have the same number of points.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	public void setDimensions(String d1, String d2) {
		this.d1 = new PolygonDimension(d1);
		this.d2 = new PolygonDimension(d2);
	}
	
	public void addPoint(double d1, double d2){
		this.d1.addPoint(d1);
		this.d2.addPoint(d2);
	}

	public void updatePoint(int index, double d1New, double d2New){
		this.d1.updatePoint(index, d1New);
		this.d2.updatePoint(index, d2New);
	}
	
	public void removePoint(int index){
		this.d1.removePoint(index);
		this.d2.removePoint(index);
	}
	
	
	public ArrayList<String> getDimensionNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add(d1.getName());
		names.add(d2.getName());
		return names;
	}

	public void setPoints(ArrayList<Double> d1Points, ArrayList<Double> d2Points) {
		this.d1.setPoints(d1Points);
		this.d2.setPoints(d2Points);
	}

	public Double[] getVertex(int index) {
		Double[] vertex = new Double[]{this.d1.getPoints().get(index), this.d2.getPoints().get(index)};
		return vertex;
	}
}
//EOF