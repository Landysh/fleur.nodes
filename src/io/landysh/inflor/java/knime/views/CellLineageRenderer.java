package io.landysh.inflor.java.knime.views;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.DataValueRenderer;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.knime.dataTypes.columnStoreCell.ColumnStoreCell;

public class CellLineageRenderer implements DataValueRenderer {

	public static final String DESCRIPTION = "Cell Lineage View";

	private static XYDataset createDataset(ColumnStore columns) {
		final XYSeriesCollection result = new XYSeriesCollection();
		final XYSeries series = new XYSeries("Random");
		final double[] x = columns.getDimensionData(columns.getColumnNames()[0]);
		final double[] y = columns.getDimensionData(columns.getColumnNames()[1]);
		for (int i = 0; i < 100; i++) {
			series.add(x[i], y[i]);
			result.addSeries(series);
		}
		return result;
	}

	@Override
	public boolean accepts(DataColumnSpec spec) {
		// I guess this checks the spec to see if it is compatible.
		if (spec.getType() == ColumnStoreCell.TYPE) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return "A descroption.";
	}

	@Override
	public Component getListCellRendererComponent(JList arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension dim = new Dimension(300, 300);
		return dim;
	}

	@Override
	public Component getRendererComponent(Object unCastCell) {
		final ColumnStoreCell cell = (ColumnStoreCell) unCastCell;
		final ColumnStore data = cell.getColumnStore();
		final XYDataset dataset = createDataset(data);
		final JFreeChart chart = ChartFactory.createScatterPlot("Foo", "bar", "foobar", dataset);
		final ChartPanel panel = new ChartPanel(chart);
		return panel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		return null;
	}
}