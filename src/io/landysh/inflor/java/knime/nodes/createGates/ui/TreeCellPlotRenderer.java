package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.AbstractFCSPlot;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotUtils;

@SuppressWarnings("serial")
public class TreeCellPlotRenderer extends DefaultTreeCellRenderer {

	private ColumnStore data;
	public TreeCellPlotRenderer(ColumnStore data) {
		this.data = data;
	}

	@Override
	public Dimension getPreferredSize() {
//		Dimension d = super.getPreferredSize();
//		d.height = height;
		return new Dimension(220,200);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getUserObject() == "Root") {
			JPanel panel = new JPanel();
			panel.setVisible(false);
			return new JPanel();
		}
		ChartSpec spec = (ChartSpec) node.getUserObject();
		AbstractFCSPlot plot = PlotUtils.createPlot(spec);
		double[] xData = data.getColumn(spec.getDomainAxisName(), spec.getDomainVectorType());
		double[] yData = data.getColumn(spec.getRangeAxisName(), spec.getRangeVectorType());
		JFreeChart chart = plot.createChart(xData, yData);
		if (!expanded) {
			chart.setBackgroundPaint(Color.LIGHT_GRAY);
		} else {
			chart.setBackgroundPaint(Color.WHITE);
		}
		if (leaf) {
			chart.setBackgroundPaint(Color.WHITE);
		}
		if (selected){
			chart.setBorderPaint(Color.LIGHT_GRAY);
			chart.setBorderVisible(true);
		}
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(200,200));
		return panel;
	}
}
// EOF