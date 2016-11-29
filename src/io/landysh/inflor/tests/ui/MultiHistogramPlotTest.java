package io.landysh.inflor.tests.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.plots.SubsetResponseChart;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.transforms.LogicleTransform;
import io.landysh.inflor.main.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class MultiHistogramPlotTest extends ApplicationFrame {

  private ChartPanel panel;
  MouseInputListener activeListener;
  //private GateCreationToolBar toolbar;

  public MultiHistogramPlotTest(String title) throws Exception {
    super(title);
    // Setup data
    String dirPath = "src/io/landysh/inflor/tests/extData/dataSet/";


    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<String>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath) == true) {
        validFiles.add(filePath);
      } else if (file.isDirectory()) {
        System.out.println("Directory " + file.getName());
      }
    }


    HashMap<String, FCSDimension> dataset = new HashMap<String, FCSDimension>();

    List<FCSFrame> fcsList = validFiles.parallelStream()
        .map(filePath -> FCSFileReader.read(filePath)).collect(Collectors.toList());
    String name = "<Pacific Blue-A>";
    for (FCSFrame fcsFile : fcsList) {
      String key = fcsFile.toString();
      FCSDimension value = FCSUtilities.findCompatibleDimension(fcsFile, name);
      dataset.put(key, value);
    }


    AbstractTransform transform = new LogicleTransform();
    SubsetResponseChart plot = new SubsetResponseChart(name, transform);
    JFreeChart chart = plot.createChart(dataset);
    panel = new ChartPanel(chart);
    JPanel editorPanel = new JPanel();
    editorPanel.add(panel);
    this.getContentPane().add(editorPanel);
  }

  public static void main(String[] args) throws Exception {
    MultiHistogramPlotTest test = new MultiHistogramPlotTest("ContourPlotTest");
    test.pack();
    test.setVisible(true);
  }
}
