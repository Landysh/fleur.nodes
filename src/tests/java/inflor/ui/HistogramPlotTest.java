package tests.java.inflor.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import main.java.inflor.core.compensation.SpilloverCompensator;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.gates.ui.GateCreationToolBar;
import main.java.inflor.core.plots.ChartSpec;
import main.java.inflor.core.plots.FCSChartPanel;
import main.java.inflor.core.plots.HistogramPlot;
import main.java.inflor.core.plots.PlotTypes;
import main.java.inflor.core.transforms.LogicleTransform;
import main.java.inflor.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class HistogramPlotTest extends ApplicationFrame {

  private FCSChartPanel panel;
  MouseInputListener activeListener;
  private GateCreationToolBar toolbar;

  public HistogramPlotTest(String title) throws Exception {
    super(title);
    // Setup data
    String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
    //String bigPath = "src/io/landysh/inflor/tests/extData/20mbFCS3.fcs";
    FCSFrame dataFrame = FCSFileReader.read(logiclePath);

    SpilloverCompensator compr = new SpilloverCompensator(dataFrame.getKeywords());
    dataFrame = compr.compensateFCSFrame(dataFrame, false);

    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.HISTOGRAM);
    spec.setDomainAxisName("PE-Texas-Red-A");
    LogicleTransform transform = new LogicleTransform();
    double[] data = FCSUtilities.findCompatibleDimension(dataFrame, "PE-Texas-Red-A").getData();
    transform.optimizeW(data);
    spec.setRangeAxisName("Count");

    HistogramPlot plot = new HistogramPlot(spec);
    JFreeChart chart = plot.createChart(dataFrame);
    panel = new FCSChartPanel(chart, spec, dataFrame);
    toolbar = new GateCreationToolBar(panel);
    panel.setSelectionListener(toolbar.getSelectionListener());
    JPanel editorPanel = new JPanel();
    editorPanel.add(panel);
    editorPanel.add(toolbar);
    this.getContentPane().add(editorPanel);
  }

  public static void main(String[] args) throws Exception {
    HistogramPlotTest test = new HistogramPlotTest("ContourPlotTest");
    test.pack();
    test.setVisible(true);
  }
}
