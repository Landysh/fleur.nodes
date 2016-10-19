package io.landysh.inflor.java.core.utils;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.gatingML.gates.AbstractGate;
import io.landysh.inflor.java.core.gatingML.gates.polygonGate.PolygonGate;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RangeGate;
import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.gateui.PolygonGateAnnotation;
import io.landysh.inflor.java.core.plots.gateui.RectangleGateAnnotation;
import io.landysh.inflor.java.core.plots.gateui.XYGateAnnotation;

public class ChartUtils {

	public static Point2D getPlotCoordinates(MouseEvent e, FCSChartPanel panel){
        Point2D p = panel.translateScreenToJava2D(e.getPoint());
        Rectangle2D plotArea = panel.getScreenDataArea();
        XYPlot plot = panel.getChart().getXYPlot();
        double x = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
        double y = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
        Point2D vertex = new Point2D.Double(x, y);
        return vertex;
    }

	public static AbstractGate createGate(XYGateAnnotation annotation) {
		AbstractGate gate = null;
		String subsetName = annotation.getSubsetName();
		String domainAxisName = annotation.getDomainAxisName();
		String rangeAxisName = annotation.getRangeAxisName();
		if (annotation instanceof RectangleGateAnnotation){
			RectangleGateAnnotation rect = (RectangleGateAnnotation) annotation;
			gate = new RangeGate(subsetName, 
				new String[]{domainAxisName, rangeAxisName} , 
				new double[] {rect.getX0(), rect.getY0()},
				new double[] {rect.getX1(), rect.getY1()});
		
		} else if (annotation instanceof PolygonGateAnnotation){
			PolygonGateAnnotation polygonGate = (PolygonGateAnnotation) annotation;
			double[] domainPoints = polygonGate.getDomainPoints();
			double[] rangePoints = polygonGate.getRangePoints();
			gate = new PolygonGate(subsetName, domainAxisName,domainPoints, rangeAxisName, rangePoints);
		}//Extend here for other gate types.
		return gate;
	}

	public static AbstractGate updateGate(AbstractGate priorGate, XYGateAnnotation updatedAnnotation, 
			String subsetLabel, String domainLabel, String rangeLable) {
		/**
		 *  creates an updated gate with prior UUID, and new coordinates.
		 */
		AbstractGate gate = null;
		if (updatedAnnotation instanceof RectangleGateAnnotation 
				&& priorGate instanceof RangeGate){
			RectangleGateAnnotation rectAnn = (RectangleGateAnnotation) updatedAnnotation;
			gate = new RangeGate(subsetLabel, 
					new String[]{domainLabel, rangeLable} , 
					new double[] {rectAnn.getX0(), rectAnn.getY0()},
					new double[] {rectAnn.getX1(), rectAnn.getY1()}, 
					priorGate.ID); 
			
		} else if (updatedAnnotation instanceof PolygonGateAnnotation 
				&& priorGate instanceof PolygonGate){
			PolygonGateAnnotation udpatedPolyAnn = (PolygonGateAnnotation) updatedAnnotation;
			gate = new PolygonGate(subsetLabel, domainLabel, udpatedPolyAnn.getDomainPoints(), 
					rangeLable, udpatedPolyAnn.getRangePoints(), priorGate.ID);
		}
		return gate;
	}
}
