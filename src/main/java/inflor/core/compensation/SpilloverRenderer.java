package main.java.inflor.core.compensation;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SpilloverRenderer {
  
  private SpilloverRenderer(){}

  public static JPanel toJPanel(SpilloverCompensator compr) {
    JPanel panel = new JPanel();
    String inDimensions = "IN: " + compr.getInputDimensions().toString();
    panel.add(new JLabel(inDimensions));
    String outDimensions = "Out: " + compr.getOutputDimensions().toString();
    panel.add(new JLabel(outDimensions));
    String spills = "Spillovers: " + compr.getSpilloverValues().toString();
    panel.add(new JLabel(spills));    
    return panel;
  }

}
