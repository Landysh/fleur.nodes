package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.ui.RectangleAnchor;

public class FakePlot extends AbstractFCSPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3697138610426712126L;
	private XYPlot plot;
	private DefaultXYZDataset dataSet;
	private ChartPanel panel;

	public FakePlot(ChartSpec spec, String priorUUID) {
		super(priorUUID, spec);
	}
	
	public FakePlot(ChartSpec spec) {
		super(null, spec);
	}

	private Paint[] createPaintScale() {
		      int nColors = 200;
		      Color [] colors = new Color[nColors];
		      float x = (float) (1./(nColors + 40.));
		      for (int i=0; i < colors.length; i++) 
		      {
		    	  colors[i] = new Color( Color.HSBtoRGB((i)*x,1.0F,1.0F));
		      }
		      return colors;
	}

	@Override
	public void update(ChartSpec spec) {
		// TODO Auto-generated method stub
	}

	@Override
	public JFreeChart createChart(double[] xData, double[] yData) {
        double min = 0;
        double max = 3;
		double[] x = {1,2,1,2,1.5};
		double[] y = {1,2,2,1,1.5};
		double[] z = {1,4,3,3,3};
		double[][] data = {x,y,z};
		this.dataSet = new DefaultXYZDataset();
		dataSet.addSeries("series1", data);
		
		//Renderer
		XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(0.1);
        renderer.setBlockHeight(0.1);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesVisible(0, true);
		Paint [] contourColors = createPaintScale();
		LookupPaintScale paintScale = new LookupPaintScale(min, max, Color.gray);
		double [] scaleValues = new double[contourColors.length];
		double delta = (max - min)/(contourColors.length -1);
		double value = min;
		for(int i=0; i<contourColors.length; i++){
			paintScale.add(value, contourColors[i]);
			scaleValues[i] = value;
			value = value + delta;
		}
		renderer.setPaintScale(paintScale);
        
        NumberAxis xAxis = new NumberAxis("FSC");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        Range range = new Range(min, max);
		xAxis.setRange(range);
		
		NumberAxis yAxis = new NumberAxis("SSC");
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		yAxis.setRange(range); 

		plot = new XYPlot();  
		plot.setDomainAxis(xAxis);
		plot.setRangeAxis(yAxis);
		plot.setRenderer(renderer);
		plot.setDataset(dataSet); 
		
		chart = new JFreeChart(plot);
		chart.removeLegend();

		return chart;
	}
}
//EOF