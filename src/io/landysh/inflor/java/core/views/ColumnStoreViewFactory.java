package io.landysh.inflor.java.core.views;

import java.awt.Color;
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

import io.landysh.inflor.java.core.dataStructures.ColumnStore;

public class ColumnStoreViewFactory {

	public static JComponent createLineageView(ColumnStore columnStore) {
		if (columnStore != null) {
			final String xName = columnStore.getColumnNames()[0];
			final String yName = columnStore.getColumnNames()[1];
			final XYSeries series = new XYSeries("");
			for (int i = 0; i < columnStore.getRowCount(); i++) {
				final double x = columnStore.getDimensionData(xName)[i];
				final double y = columnStore.getDimensionData(yName)[i];
				series.add(x, y);
			}

			final XYSeriesCollection data = new XYSeriesCollection(series);
			final String title = "My First FCS Plot";
			final String xAxisLabel = xName;
			final String yAxisLabel = yName;
			final JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, data,
					PlotOrientation.VERTICAL, false, false, false);
			final XYPlot xyPlot = (XYPlot) chart.getPlot();
			final XYItemRenderer renderer = xyPlot.getRenderer();
			renderer.setSeriesPaint(0, new Color(0.0f, 1.0f, 0.0f, 0.5f));
			final Ellipse2D circle = new Ellipse2D.Double(1, 1, 1, 1);
			renderer.setSeriesShape(0, circle);
			chart.setBackgroundPaint(Color.WHITE);

			final ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPreferredSize(new java.awt.Dimension(300, 300));
			chartPanel.setName("Cell Lineage View");
			return chartPanel;
		} else {
			throw new NullPointerException("Input data null.");
		}

	}

	public static JComponent createResponseView(ColumnStore columnStore) {
		return null;
	}

	public ColumnStoreViewFactory() {
	}
}
