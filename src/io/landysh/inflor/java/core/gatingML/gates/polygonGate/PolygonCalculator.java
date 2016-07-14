package io.landysh.inflor.java.core.gatingML.gates.polygonGate;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonCalculator {
	
	Polygon gate;
	Point   event;
	GeometryFactory factory;

	public PolygonCalculator(ArrayList<Double> D1, ArrayList<Double> D2) {
		if (D1.size() != D2.size()) {
			String message = "d1 and d2 must have the name size";
			IllegalArgumentException e = new IllegalArgumentException(message);
			e.printStackTrace();
			throw e;
		}
		factory = new GeometryFactory();
		Coordinate[] points = new Coordinate[D1.size()];
		for (int i=0;i<points.length;i++){
			points[i] = new Coordinate(D1.get(i), D2.get(i));
		}
		gate = factory.createPolygon(points);
		event = factory.createPoint( new Coordinate());
	}

	public boolean isInside(double x, double y) {
		event.getCoordinate().x = x;
		event.getCoordinate().y = y;
		return gate.contains(event);
	}
}
