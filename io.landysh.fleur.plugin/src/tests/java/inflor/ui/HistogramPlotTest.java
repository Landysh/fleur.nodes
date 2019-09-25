package inflor.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import fleur.core.compensation.SpilloverCompensator;
import fleur.core.data.FCSFrame;
import fleur.core.fcs.FCSFileReader;
import fleur.core.gates.ui.GateCreationToolBar;
import fleur.core.plots.ChartSpec;
import fleur.core.plots.FCSChartPanel;
import fleur.core.plots.HistogramPlot;
import fleur.core.plots.PlotTypes;
import fleur.core.transforms.LogicleTransform;
import fleur.core.transforms.TransformSet;
import fleur.core.utils.FCSUtilities;

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
    LogicleTransform transform = new LogicleTransform(262144);
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
