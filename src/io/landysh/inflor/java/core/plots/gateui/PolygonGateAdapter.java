package io.landysh.inflor.java.core.plots.gateui;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;

import org.jfree.chart.annotations.XYLineAnnotation;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.utils.ChartUtils;

public class PolygonGateAdapter extends MouseInputAdapter {
    private FCSChartPanel panel;
	private PolygonGateAnnotation currentPoly;
	private ArrayList<Point2D> vertices = new ArrayList<>();
	private ArrayList<XYLineAnnotation> segments;
	private Point2D anchorPoint;
	private XYLineAnnotation anchorSegment;
	
    public PolygonGateAdapter(FCSChartPanel panel) {
        this.panel = panel;
    }
    
    
	@Override
    public void mouseClicked(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==1){
    		Point2D v = ChartUtils.getPlotCoordinates(e, panel);
    		anchorPoint = v;
    		vertices.add(v);
    		updateTemporaryAnnotation();
    	} else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2){
    		//Finish the polygon and ask for a name
    		int pointCount = vertices.size()*2;
    		double[] polygon = new double[pointCount];
    		for (int i=0;i<pointCount;i++ ){
    			polygon[i] = vertices.get(i/2).getX();
    			polygon[i+1] = vertices.get(i/2).getY();
    			i++;
    		}
    		PolygonGateAnnotation updatedPoly = new PolygonGateAnnotation(polygon);
    		ChartUtils.updateXYAnnotation(currentPoly, updatedPoly, panel);
    		currentPoly = updatedPoly;
    		
    		//remove the anchor point
    		anchorPoint = null;
    	}
    }

	@Override
    public void mouseMoved(MouseEvent e) {
		if (anchorSegment != null){
			panel.getChart().getXYPlot().removeAnnotation(anchorSegment);
		}
		if (anchorPoint!=null){
			Point2D p = ChartUtils.getPlotCoordinates(e, panel);
			anchorSegment = new XYLineAnnotation(anchorPoint.getX(), anchorPoint.getY(), p.getX(), p.getY());
			panel.getChart().getXYPlot().addAnnotation(anchorSegment);
		}
	}
	

	private void updateTemporaryAnnotation() {
		Point2D previousVertex = null;
		
		if (segments!=null){
			segments
				.stream()
				.forEach(segment -> panel.getChart().getXYPlot().removeAnnotation(segment));
		}
		segments = new ArrayList<XYLineAnnotation>();
		for (Point2D v:vertices){
			if (previousVertex==null){
				previousVertex = v;
			} else {
				segments.add(new XYLineAnnotation(previousVertex.getX(), previousVertex.getY(), v.getX(), v.getY()));
				previousVertex = v;
			}
		}
		segments
			.stream()
			.forEach(segment -> panel.getChart().getXYPlot().addAnnotation(segment));
	} 
}
