package io.landysh.inflor.java.core.gatingML;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Element;

public class PolygonGate extends AbstractGate {
	
	PolygonDimension d1;
	PolygonDimension d2;
	
	public PolygonGate(String id) {
		super(id);
	}

	@Override
	boolean[] evaluate(Hashtable<String, double[]> data, int rowCount) {
		validateWithData(data.keySet());
		PolygonCalculator poly = new PolygonCalculator(d1.points, d2.points);
		boolean[] result = new boolean[rowCount];
		double[] d1Data = data.get(d1.getName());
		double[] d2Data = data.get(d2.getName());
		for (int i=0;i<d1Data.length;i++){
			if(poly.isInside(d1Data[i], d2Data[i])==true){
				result[i] = true;
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	private void validateWithData(Set<String> keySet) {
		if (keySet.contains(d1.getName()) == true 
				&& keySet.contains(d2.getName())==true){
			validate();
		}
	}

	@Override
	public Element toXMLElement() {
		//TODO
		return null;
	}

	@Override
	public void validate() throws IllegalStateException {
		if (getVertexCount()<3){
			String message = "A polygon requires at least 3 verticies!";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
		
		if (d1.getPoints().size() != d2.getPoints().size()){
			String message = "A polygon requires the same number of points in both dimensions.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	public int getVertexCount() {
		if (d1.points!=null && d2.points!=null && d1.points.size() == d2.points.size()){
			return d1.points.size();
		} else {
			String message = "both dimensions must both be initialized and have the same number of points.";
			IllegalStateException ise = new IllegalStateException(message);
			ise.printStackTrace();
			throw ise;
		}
	}

	public PolygonDimension getD2() {
		return d2;
	}

	public void setD2(PolygonDimension dimension) {
		this.d2 = dimension;
	}

	public PolygonDimension getD1() {
		return d1;
	}

	public void setD1(PolygonDimension dimension) {
		this.d1 = dimension;
	}

	@Override
	public ArrayList<String> getDimensionNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add(d1.getName());
		names.add(d2.getName());
		return names;
	}
}
