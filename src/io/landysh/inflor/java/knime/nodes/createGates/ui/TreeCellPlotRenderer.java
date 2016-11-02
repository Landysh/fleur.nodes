package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.gates.GateUtilities;
import io.landysh.inflor.java.core.plots.AbstractFCChart;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotUtils;
import io.landysh.inflor.java.core.utils.BitSetUtils;
import io.landysh.inflor.java.core.utils.FCSUtils;

@SuppressWarnings("serial")
public class TreeCellPlotRenderer extends DefaultTreeCellRenderer {


  public TreeCellPlotRenderer() {}

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    FCSFrame dataFrame = (FCSFrame) node.getUserObjectPath()[0];
    List<AbstractGate> gates = extractGates(node.getUserObjectPath());
    BitSet mask = GateUtilities.applyGatingPath(dataFrame, gates);

    //node is root
    if (node.getUserObject() instanceof FCSFrame) {
      FCSFrame root = (FCSFrame) node.getUserObject();
      JLabel label = new JLabel(root.getPrefferedName() + " " + root.getRowCount() , LEFT);
      return label;
    //node is a gate
    } else if (node.getUserObject() instanceof AbstractGate) {
      dataFrame = (FCSFrame) node.getUserObjectPath()[0];
      String label = BitSetUtils.frequencyOfParent(mask, 2);
      JLabel jLabel = new JLabel(label, LEFT);
      return jLabel;
    //node is plot
    } else if (node.getUserObject() instanceof ChartSpec) {
      FCSFrame filteredFrame = FCSUtils.filterColumnStore(mask, dataFrame);
      ChartSpec spec = (ChartSpec) node.getUserObject();
      AbstractFCChart plot = PlotUtils.createPlot(spec);
      JFreeChart chart = plot.createChart(filteredFrame);
      if (!expanded) {
        chart.setBackgroundPaint(Color.LIGHT_GRAY);
      } else {
        chart.setBackgroundPaint(Color.WHITE);
      }
      if (leaf) {
        chart.setBackgroundPaint(Color.WHITE);
      }
      if (selected) {
        chart.setBorderPaint(Color.LIGHT_GRAY);
        chart.setBorderVisible(true);
      }
      ChartPanel panel = new ChartPanel(chart);
      panel.setPreferredSize(new Dimension(220, 200));
      return panel;
    } else {
      RuntimeException e = new RuntimeException("Tree node type not supported");
      e.printStackTrace();
      throw e;
    }
  }

  private List<AbstractGate> extractGates(Object[] userObjectPath) {
    List<AbstractGate> gates = new ArrayList<AbstractGate>();
    for (Object o:userObjectPath){
      if (o instanceof AbstractGate){
        gates.add((AbstractGate) o);
      }
    }
    return gates;
  }
}
