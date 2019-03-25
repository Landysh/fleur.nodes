package inflor.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import inflor.core.compensation.SpilloverCompensator;
import inflor.core.data.FCSFrame;
import inflor.core.fcs.FCSFileReader;
import inflor.core.gates.ui.GateCreationToolBar;
import inflor.core.plots.ChartSpec;
import inflor.core.plots.FCSChartPanel;
import inflor.core.plots.HistogramPlot;
import inflor.core.plots.PlotTypes;
import inflor.core.transforms.LogicleTransform;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.FCSUtilities;

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
    double[] data = FCSUtilities.findCompatibleDimension(dataFrame, "PE-Texas-Red-A").get().getData();
    transform.optimize(data);
    spec.setRangeAxisName("Count");

    HistogramPlot plot = new HistogramPlot(spec);
    JFreeChart chart = plot.createChart(dataFrame, new TransformSet());
    panel = new FCSChartPanel(chart, spec, dataFrame, new TransformSet());
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
