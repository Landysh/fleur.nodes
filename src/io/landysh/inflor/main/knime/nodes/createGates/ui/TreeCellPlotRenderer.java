package io.landysh.inflor.main.knime.nodes.createGates.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jfree.chart.JFreeChart;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.gates.AbstractGate;
import io.landysh.inflor.main.core.gates.GateUtilities;
import io.landysh.inflor.main.core.plots.AbstractFCChart;
import io.landysh.inflor.main.core.plots.ChartSpec;
import io.landysh.inflor.main.core.plots.FCSChartPanel;
import io.landysh.inflor.main.core.plots.PlotUtils;
import io.landysh.inflor.main.core.utils.BitSetUtils;
import io.landysh.inflor.main.core.utils.ChartUtils;
import io.landysh.inflor.main.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class TreeCellPlotRenderer extends DefaultTreeCellRenderer {
  
  String[] columnNames = new String[] {"Name", "Count", "Frequency of Parent"};

  public TreeCellPlotRenderer() {}

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    if (node instanceof DefaultMutableTreeNode){
      node.breadthFirstEnumeration();
    }
    
    Object uo = node.getUserObjectPath()[0];
    FCSFrame dataFrame = null;
    BitSet mask = null;
    if (uo instanceof FCSFrame){
      dataFrame = (FCSFrame) node.getUserObjectPath()[0];
      List<AbstractGate> gates = extractGates(node.getUserObjectPath());
      mask = GateUtilities.applyGatingPath(dataFrame, gates);
    }

    //node is root
    if (node.getUserObject() instanceof FCSFrame&&dataFrame!=null) {
      String[][] tableRow = new String[][]{{"Ungated", Integer.toString(dataFrame.getRowCount()), "-"}};
      JTable table = new JTable(tableRow, columnNames);
      formatTable(selected, expanded, leaf, table);
      return table;
    //node is a gate
    } else if (node.getUserObject() instanceof AbstractGate&&mask!=null) {
      AbstractGate gate = (AbstractGate) node.getUserObject();
      String[][] tableRow = new String[][]{{gate.getLabel(), Integer.toString(mask.cardinality()), BitSetUtils.frequencyOfParent(mask, 2)}};
      JTable table = new JTable(tableRow, columnNames);
      formatTable(selected, expanded, leaf, table);
      return table;
    //node is plot
    } else if (node.getUserObject() instanceof ChartSpec && mask!=null) {
      FCSFrame filteredFrame = FCSUtilities.filterColumnStore(mask, dataFrame);
      ChartSpec spec = (ChartSpec) node.getUserObject();
      AbstractFCChart plot = PlotUtils.createPlot(spec);
      JFreeChart chart = plot.createChart(filteredFrame);
      formatChart(selected, expanded, leaf, chart);
      FCSChartPanel panel = new FCSChartPanel(chart, spec, filteredFrame);
      List<AbstractGate> siblingGates = findSiblingGates(node);
      siblingGates
        .stream()
        .filter(gate -> ChartUtils.gateIsCompatibleWithChart(gate, spec))
        .map(gate -> ChartUtils.createAnnotation(gate))
        .forEach(ann -> panel.createGateAnnotation(ann));
      panel.setPreferredSize(new Dimension(220, 200));
      return panel;
    } else {
      return new JLabel("Unsupported node type.");//TODO: Issue with some incorrect model during init?
    }
  }

  private void formatTable(boolean selected, boolean expanded, boolean leaf,
      JTable table) {
    if (selected){
      table.setRowSelectionInterval(0, 0);
    }
  }

  private void formatChart(boolean selected, boolean expanded, boolean leaf, JFreeChart chart) {
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
  }

  private List<AbstractGate> findSiblingGates(DefaultMutableTreeNode node) {
    List<AbstractGate> gates = new ArrayList<>();
    int siblingCount = node.getParent().getChildCount();
    for (int i=0;i<siblingCount;i++){
      DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) node.getParent().getChildAt(i); 
      if (siblingNode.getUserObject() instanceof AbstractGate){
        gates.add((AbstractGate)siblingNode.getUserObject());
      }
    }
    return gates;
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
