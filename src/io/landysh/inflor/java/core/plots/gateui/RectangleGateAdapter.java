package io.landysh.inflor.java.core.plots.gateui;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.jfree.chart.plot.XYPlot;

import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RangeGate;
import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.ui.DefaultGraphics;
import io.landysh.inflor.java.core.utils.ChartUtils;
import io.landysh.inflor.java.knime.nodes.createGates.ui.GateNameEditor;

public class  RectangleGateAdapter extends MouseInputAdapter{
    private  final FCSChartPanel panel;
	private Point2D vert1;
	private Point2D vert0;
	private RectangleGateAnnotation rectangleAnnotation;
	private double x0;
	private double x1;
	private double y0;
	private double y1;

    public RectangleGateAdapter(FCSChartPanel panel) {
        this.panel = panel;
    }

    private void updateMarker(){
        if (rectangleAnnotation!=null){
            panel.getChart().getXYPlot().removeAnnotation(rectangleAnnotation);
        }
        if (!(vert0==null&&vert1==null)){
            x0 = Math.min(vert0.getX(), vert1.getX());
            x1 = Math.max(vert0.getX(), vert1.getX());
            y0 = Math.min(vert0.getY(), vert1.getY());
            y1 = Math.max(vert0.getY(), vert1.getY());
        	rectangleAnnotation = new RectangleGateAnnotation(x0,y0,x1,y1, DefaultGraphics.SELECTED_STROKE, DefaultGraphics.DEFAULT_GATE_COLOR);
        	((XYPlot) panel.getChart().getPlot()).addAnnotation(rectangleAnnotation);
        }
    }


    
    @Override
    public void mouseReleased(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1){
    		vert1 = ChartUtils.getPlotCoordinates(e, panel);
    		updateMarker();
    	
    		//Pop a gate editor dialog
    		Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(panel);

    		GateNameEditor dialog = new GateNameEditor(topFrame);
    		dialog.setVisible(true);
    		if (dialog.isOK) {
    			String name = dialog.getGateName();
    			String xName = panel.getChart().getXYPlot().getDomainAxis().getLabel();
    			String yName = panel.getChart().getXYPlot().getRangeAxis().getLabel();
    			
    			RangeGate gate = new RangeGate(name, 
    					new String[]{xName, yName} , 
    					new double[] {x0, y0}, 
    					new double[] {x1, y1});
    			String label = panel.addRectangleGate(gate);
    			rectangleAnnotation.setToolTipText(label);
    			rectangleAnnotation.setNotify(true);
    		} else {
                panel.getChart().getXYPlot().removeAnnotation(rectangleAnnotation);
    		}
			rectangleAnnotation = null;
    		dialog.dispose();    	
    	}
    }

	@Override
    public void mousePressed(MouseEvent e) {
    	if (e.getButton() == MouseEvent.BUTTON1){
    		vert0 = ChartUtils.getPlotCoordinates(e, panel);
    	}		
    }
    
	@Override
    public void mouseDragged(MouseEvent e){
    	if (SwingUtilities.isLeftMouseButton(e)){
    		vert1 = ChartUtils.getPlotCoordinates(e, panel);
    		updateMarker();
    	}
    }
}
