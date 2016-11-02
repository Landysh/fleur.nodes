package io.landysh.inflor.tests.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.compensation.SpilloverCompensator;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.gates.ui.GateCreationToolBar;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.utils.FCSUtils;

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
    dataFrame = compr.compensateFCSFrame(dataFrame);

    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.Histogram);
    spec.setDomainAxisName("PE-Texas-Red-A");
    LogicleTransform transform = new LogicleTransform();
    double[] data = FCSUtils.findCompatibleDimension(dataFrame, "PE-Texas-Red-A").getData();
    transform.optimizeW(data);
    spec.setDomainTransform(transform);
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
