package main.java.inflor.core.compensation;

import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SpilloverRenderer {
  
  private SpilloverRenderer(){}

  public static JPanel toJPanel(SpilloverCompensator compr) {
    JPanel panel = new JPanel();
    String inDimensions = "IN: " + Arrays.toString(compr.getInputDimensions());
    panel.add(new JLabel(inDimensions));
    String outDimensions = "Out: " + Arrays.toString(compr.getOutputDimensions());
    panel.add(new JLabel(outDimensions));
    String spills = "Spillovers: " + Arrays.toString(compr.getSpilloverValues());
    panel.add(new JLabel(spills));    
    return panel;
  }

}
