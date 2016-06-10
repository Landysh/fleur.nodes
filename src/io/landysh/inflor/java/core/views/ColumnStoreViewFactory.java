package io.landysh.inflor.java.core.views;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import io.landysh.inflor.java.core.ColumnStore;

public class ColumnStoreViewFactory {

	public ColumnStoreViewFactory() {}

	public static JComponent createLineageView(ColumnStore columnStore) {
		String xName = columnStore.getColumnNames()[0];
		String yName = columnStore.getColumnNames()[1];
		final XYSeries series = new XYSeries("");
		for (int i=0;i<columnStore.getRowCount();i++){
			double x = columnStore.getColumn(xName)[i];
			double y = columnStore.getColumn(yName)[i];
	        series.add(x,y);
		}
		
        final XYSeriesCollection data = new XYSeriesCollection(series);
        String title = "My First FCS Plot";
		String xAxisLabel = xName;
		String yAxisLabel = yName;
		final JFreeChart chart = ChartFactory.createScatterPlot(
				title,
				xAxisLabel,
				yAxisLabel,
				data,
				PlotOrientation.VERTICAL,
				false,
				false,
				false
				);
        XYPlot xyPlot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0.0f, 1.0f, 0.0f, 0.5f));
        Ellipse2D circle = new Ellipse2D.Double(1, 1, 1, 1);
        renderer.setSeriesShape(0, circle);
		chart.setBackgroundPaint(Color.WHITE);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(300,300));
        chartPanel.setName("Cell Lineage View");
		return chartPanel;
	}

	public static JComponent createResponseView(ColumnStore columnStore) {
		return null;
	}

}
