package io.landysh.inflor.java.core.plots;

/* ----------------------
* XYBlockChartDemoColor.java
* ----------------------
* (C) Copyright 2006, 2007, by Object Refinery Limited.
*
*/



import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

/**
* A simple demonstration application showing a chart created using
* the {@link XYBlockRenderer} with color contours.
*/
public class XYBlockChartDemoColor extends ApplicationFrame {

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


/**
    * Constructs the demo application.
    *
    * @param title  the frame title.
    */
   public XYBlockChartDemoColor(String title) {
      super(title);
      JPanel chartPanel = createDemoPanel();
      chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
      setContentPane(chartPanel);
   }

   /**
    * Creates a sample chart.
    *
    * @param dataset  the dataset.
    *
    * @return A sample chart.
    */
   public static JFreeChart createChart(XYZDataset dataset) {
      double min = -1;
      double max = 1;
      NumberAxis xAxis = new NumberAxis("X");
      xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      xAxis.setLowerMargin(0.0);
      xAxis.setUpperMargin(0.0);
      xAxis.setAxisLinePaint(Color.white);
      xAxis.setTickMarkPaint(Color.white);

      NumberAxis yAxis = new NumberAxis("Y");
      yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      yAxis.setLowerMargin(0.0);
      yAxis.setUpperMargin(0.0);
      yAxis.setAxisLinePaint(Color.white);
      yAxis.setTickMarkPaint(Color.white);
      XYBlockRenderer renderer = new XYBlockRenderer();

      LookupPaintScale scale = new LookupPaintScale(-1, 1, Color.gray);
      Paint [] contourColors = getFullRainBowScale();
      double [] scaleValues = new double[contourColors.length];
      double delta = (max - min)/(contourColors.length -1);
      double value = min;
      for(int i=0; i<contourColors.length; i++)
      {
         scale.add(value, contourColors[i]);
         scaleValues[i] = value;
         value = value + delta;
      }

      renderer.setPaintScale(scale);

      XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
      plot.setBackgroundPaint(Color.lightGray);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.white);
      plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
      plot.setOutlinePaint(Color.blue);
      JFreeChart chart = new JFreeChart("XYBlockChartDemo1", plot);
      chart.removeLegend();
      NumberAxis scaleAxis = new NumberAxis("Scale");
      scaleAxis.setRange(min, max);
      scaleAxis.setAxisLinePaint(Color.white);
      scaleAxis.setTickMarkPaint(Color.white);
      scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
      PaintScaleLegend legend = new PaintScaleLegend(scale, scaleAxis);//new PaintScaleLegend(new GrayPaintScale(), scaleAxis);
      legend.setStripOutlineVisible(false);
      //legend.setSubdivisionCount(20);
      legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      legend.setAxisOffset(5.0);
      legend.setMargin(new RectangleInsets(5, 5, 5, 5));
      legend.setFrame(new BlockBorder(Color.red));
      legend.setPadding(new RectangleInsets(10, 10, 10, 10));
      legend.setStripWidth(10);
      legend.setPosition(RectangleEdge.LEFT);
      //legend.setBackgroundPaint(new Color(120, 120, 180));
      chart.addSubtitle(legend);
      //chart.setBackgroundPaint(new Color(180, 180, 250));
      ChartUtilities.applyCurrentTheme(chart);
      return chart;
   }
   /**
    * Sets a color scale (red-yellow-green-blue-magenta) 
    * to be used for contour plotting.
    * 
    * @return  The colors in the rainbow color spectrum.
    */
   private static Paint[] getFullRainBowScale() 
   {
      // minimum of about 200 to not have perceptible steps in color scale
      // whether or not perceptible color gradients show depend upon the
      // legend size and the monitor settings
      int ncolor = 360;
      Color [] rainbow = new Color[ncolor];
      // divide the color wheel up into more than ncolor pieces
      // but don't go all of the way around the wheel, or the first color
      // will repeat.  The 60 value is about a minimum of 40, or the
      // red color will repeat.  Too large a value, and there will be no magenta.
      float x = (float) (1./(ncolor + 60.));
      for (int i=0; i < rainbow.length; i++) 
      {
         rainbow[i] = new Color( Color.HSBtoRGB((i)*x,1.0F,1.0F));
      }
      return rainbow;
   }



   /**
    * Creates a sample dataset.
    *
    * @return A dataset.
    */
   public static XYZDataset createDataset() {
      return new XYZDataset() {
         public int getSeriesCount() {
            return 1;
         }
         public int getItemCount(int series) {
            return 10000;
         }
         public Number getX(int series, int item) {
            return new Double(getXValue(series, item));
         }
         public double getXValue(int series, int item) {
            return item / 100 - 50;
         }
         public Number getY(int series, int item) {
            return new Double(getYValue(series, item));
         }
         public double getYValue(int series, int item) {
            return item - (item / 100) * 100 - 50;
         }
         public Number getZ(int series, int item) {
            return new Double(getZValue(series, item));
         }
         public double getZValue(int series, int item) {
            double x = getXValue(series, item);
            double y = getYValue(series, item);
            return Math.sin(Math.sqrt(x * x + y * y) / 5.0);
         }
         public void addChangeListener(DatasetChangeListener listener) {
            // ignore - this dataset never changes
         }
         public void removeChangeListener(DatasetChangeListener listener) {
            // ignore
         }
         public DatasetGroup getGroup() {
            return null;
         }
         public void setGroup(DatasetGroup group) {
            // ignore
         }
         public Comparable<String> getSeriesKey(int series) {
            return "sin(sqrt(x + y))";
         }
         public int indexOf(@SuppressWarnings("rawtypes") Comparable seriesKey) {
            return 0;
         }
         public DomainOrder getDomainOrder() {
            return DomainOrder.ASCENDING;
         }
      };
   }

   /**
    * Creates a panel for the demo.
    *
    * @return A panel.
    */
   public static JPanel createDemoPanel() {
      return new ChartPanel(createChart(createDataset()));
   }


   /**
    * Starting point for the demonstration application.
    *
    * @param args  ignored.
    */
   public static void main(String[] args) {
      XYBlockChartDemoColor demo = new XYBlockChartDemoColor(
            "JFreeChart: XYBlockChartDemoColor");
      demo.pack();
      RefineryUtilities.centerFrameOnScreen(demo);
      demo.setVisible(true);
   }
}