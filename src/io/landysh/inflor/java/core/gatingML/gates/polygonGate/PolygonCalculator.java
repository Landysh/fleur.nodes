package io.landysh.inflor.java.core.gatingML.gates.polygonGate;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

public class PolygonCalculator {

	Polygon gate;
	Point event;
	GeometryFactory factory;

	public PolygonCalculator(ArrayList<Double> D1, ArrayList<Double> D2) {
		if (D1.size() != D2.size() && D1.size() >= 3) {
			final String message = "d1 and d2 must have the name size";
			final IllegalArgumentException e = new IllegalArgumentException(message);
			e.printStackTrace();
			throw e;
		}
		factory = new GeometryFactory();

		final ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		for (int i = 0; i < D1.size(); i++) {
			points.add(new Coordinate(D1.get(i), D2.get(i)));
		}
		// Close the loop manually. Maybe a better way?
		points.add(new Coordinate(D1.get(0), D2.get(0)));

		final CoordinateArraySequence coords = new CoordinateArraySequence(
				points.toArray(new Coordinate[points.size()]));
		// LinearRing ring = factory.createLinearRing();

		gate = factory.createPolygon(coords);
		event = factory.createPoint(new Coordinate(0, 0));
	}

	public boolean isInside(double x, double y) {
		event.getCoordinate().x = x;
		event.getCoordinate().y = y;
		return gate.contains(event);
	}
}
// EOF