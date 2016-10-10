package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.plots.AbstractFCChart;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotUtils;
import io.landysh.inflor.java.core.subsets.AbstractSubset;
import io.landysh.inflor.java.core.subsets.RootSubset;
import io.landysh.inflor.java.core.utils.FCSUtils;

@SuppressWarnings("serial")
public class TreeCellPlotRenderer extends DefaultTreeCellRenderer {

	private ColumnStore data;
	public TreeCellPlotRenderer(ColumnStore data) {
		this.data = data;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getUserObject() instanceof AbstractSubset) {
			RootSubset root = (RootSubset) node.getUserObject();
			JLabel label = new JLabel(root.toString(), LEFT);
			return label;
		} else if (node.getUserObject() instanceof ChartSpec){
			ChartSpec spec = (ChartSpec) node.getUserObject();
			AbstractFCChart plot = PlotUtils.createPlot(spec);
			
			FCSDimension domainDimension = FCSUtils.findCompatibleDimension(data.getData(), spec.getDomainAxisName());
			FCSDimension rangeDimension = FCSUtils.findCompatibleDimension(data.getData(), spec.getRangeAxisName());

			double[] xData = domainDimension.getData();
			double[] yData = rangeDimension.getData();
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
			panel.setPreferredSize(new Dimension(220,200));
			return panel;
		} else {
			RuntimeException e = new RuntimeException("Tree node type not supported");
			e.printStackTrace();
			throw e;
		}
	}
}
