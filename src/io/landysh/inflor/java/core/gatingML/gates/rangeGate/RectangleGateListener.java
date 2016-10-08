package io.landysh.inflor.java.core.gatingML.gates.rangeGate;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.ui.DefaultGraphics;

public class  RectangleGateListener extends MouseInputAdapter{
    //http://stackoverflow.com/questions/16105277/jfreechart-select-an-area-without-zooming
    private  final FCSChartPanel panel;
	private Point2D vert1;
	private Point2D vert0;
	private XYBoxAnnotation box;
	private double x0;
	private double x1;
	private double y0;
	private double y1;

    public RectangleGateListener(FCSChartPanel panel) {
        this.panel = panel;
    }

    private void updateMarker(){
        if (box != null){
            ((XYPlot) panel.getChart().getPlot()).removeAnnotation(box);
        }
        if (!(vert0==null&&vert1==null)){
            x0 = Math.min(vert0.getX(), vert1.getX());
            x1 = Math.max(vert0.getX(), vert1.getX());
            y0 = Math.min(vert0.getY(), vert1.getY());
            y1 = Math.max(vert0.getY(), vert1.getY());
        	box = new XYBoxAnnotation(x0,y0,x1,y1, DefaultGraphics.SELECTED_STROKE, DefaultGraphics.DEFAULT_GATE_COLOR);
        	((XYPlot) panel.getChart().getPlot()).addAnnotation(box);
        }
    }

    private Point2D getPosition(MouseEvent e){
        Point2D p = panel.translateScreenToJava2D(e.getPoint());
        Rectangle2D plotArea = panel.getScreenDataArea();
        XYPlot plot = (XYPlot) panel.getChart().getPlot();
        double x = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
        double y = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
        Point2D vertex = new Point2D.Double(x, y);
        return vertex;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1){
    		vert1 = getPosition(e);
    		updateMarker();
    	
    		//Pop a gate editor dialog
    		Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(panel);

    		GateEditorDialog dialog = new GateEditorDialog(topFrame);
    		dialog.setVisible(true);
    		if (dialog.isOK) {
    			String name = dialog.getGateName();
    			String xName = panel.getChart().getXYPlot().getDomainAxis().getLabel();
    			String yName = panel.getChart().getXYPlot().getRangeAxis().getLabel();
    			
    			RangeGate gate = new RangeGate(name, 
    					new String[]{xName, yName} , 
    					new double[] {x0, y0}, 
    					new double[] {x1, y1});
    			panel.addGate(gate);
    		}
    		dialog.dispose();    	
    	}
    }

	@Override
    public void mousePressed(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1){
    		vert0 = getPosition(e);
    	}		
    }
    
	@Override
    public void mouseDragged(MouseEvent e){
    	if (SwingUtilities.isLeftMouseButton(e)){
    		vert1 = getPosition(e);
    		updateMarker();
    	}
    }
}
