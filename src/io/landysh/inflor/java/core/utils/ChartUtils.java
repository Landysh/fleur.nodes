package io.landysh.inflor.java.core.utils;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.gateui.XYGateAnnotation;

public class ChartUtils {
	public static Point2D getPlotCoordinates(MouseEvent e, ChartPanel panel){
        Point2D p = panel.translateScreenToJava2D(e.getPoint());
        Rectangle2D plotArea = panel.getScreenDataArea();
        XYPlot plot = panel.getChart().getXYPlot();
        double x = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
        double y = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
        Point2D vertex = new Point2D.Double(x, y);
        return vertex;
    }

	public static void updateXYAnnotation(XYGateAnnotation oldAnn,
			XYGateAnnotation newAnn, FCSChartPanel panel) {
		if (oldAnn!=null){
			panel.getChart().getXYPlot().removeAnnotation(oldAnn);
		}
		panel.getChart().getXYPlot().addAnnotation(newAnn);
	}
}
