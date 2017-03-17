package inflor.ui;


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

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.fcs.FCSFileReader;
import inflor.core.plots.CategoryResponseChart;
import inflor.core.transforms.AbstractTransform;
import inflor.core.transforms.LogicleTransform;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class MultiHistogramPlotTest extends ApplicationFrame {

  private ChartPanel panel;
  MouseInputListener activeListener;
  // private GateCreationToolBar toolbar;

  public MultiHistogramPlotTest(String title) throws Exception {
    super(title);
    // Setup data
    String dirPath = "src/io/landysh/inflor/tests/extData/dataSet/";


    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<String>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath)) {
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
      FCSDimension value = FCSUtilities.findCompatibleDimension(fcsFile, name).get();
      dataset.put(key, value);
    }

    AbstractTransform transform = new LogicleTransform();
    CategoryResponseChart plot = new CategoryResponseChart(name, transform);
    FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(fcsList, 1000);
    JFreeChart chart = plot.createChart(summaryFrame);
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
