package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.GrayPaintScale;
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

	public FakePlot(String priorUUID) {
		super(priorUUID);

		double[] x = {1,2,1,2,1.5};
		double[] y = {1,2,2,1,1.5};
		double[] z = {1,4,3,3,3};
		
		double[][] data = {x,y,z};
		
		DefaultXYZDataset dataSet = new DefaultXYZDataset();
		dataSet.addSeries("series1", data);
		XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setBlockWidth(0.1);
        renderer.setBlockHeight(0.1);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesVisible(0, true);
		
	      double min = 0;
	      double max = 3;
	      NumberAxis xAxis = new NumberAxis("FSC");
	      xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	      xAxis.setLowerMargin(0.0);
	      xAxis.setUpperMargin(0.0);
	      xAxis.setAxisLinePaint(Color.white);
	      xAxis.setTickMarkPaint(Color.white);
	      Range range = new Range(min, max);
		  xAxis.setRange(range);
	      
	      NumberAxis yAxis = new NumberAxis("SSC");
	      yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	      yAxis.setLowerMargin(0.0);
	      yAxis.setUpperMargin(0.0);
	      yAxis.setAxisLinePaint(Color.white);
	      yAxis.setTickMarkPaint(Color.white);
	      yAxis.setRange(range);
	      
	      LookupPaintScale paintScale = new LookupPaintScale(min, max, Color.gray);
	      Paint [] contourColors = createPaintScale();
	      double [] scaleValues = new double[contourColors.length];
	      double delta = (max - min)/(contourColors.length -1);
	      double value = min;
	      for(int i=0; i<contourColors.length; i++)
	      {
	    	 paintScale.add(value, contourColors[i]);
	         scaleValues[i] = value;
	         value = value + delta;
	      }

	      renderer.setPaintScale(paintScale);

		
	      
	    this.setDomainAxis(xAxis);
	    this.setRangeAxis(yAxis);
		this.setRenderer(renderer);
		this.setDataset(dataSet); 
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
	public void update(PlotSpec spec) {
		this.setBackgroundAlpha(0.5f);
	}
}
