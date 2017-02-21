package tests.java.inflor.ui;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.gates.ui.GateCreationToolBar;
import main.java.inflor.core.plots.ChartSpec;
import main.java.inflor.core.plots.DensityPlot;
import main.java.inflor.core.plots.FCSChartPanel;
import main.java.inflor.core.plots.PlotTypes;

@SuppressWarnings("serial")
public class ContourPlotFrame extends ApplicationFrame {

  private FCSChartPanel panel;
  transient MouseInputListener activeListener;
  private GateCreationToolBar toolbar;

  public ContourPlotFrame(String title) {
    super(title);
    // Setup data
    String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
    final FCSFrame dataStore = FCSFileReader.read(logiclePath);

    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.DENSITY);
    spec.setDomainAxisName("FSC-A");
    spec.setRangeAxisName("SSC-A");

    DensityPlot plot = new DensityPlot(spec);
    JFreeChart chart = plot.createChart(dataStore);
    panel = new FCSChartPanel(chart, spec, dataStore);
    toolbar = new GateCreationToolBar(panel);
    panel.setSelectionListener(toolbar.getSelectionListener());
    JPanel editorPanel = new JPanel();
    editorPanel.add(panel);
    editorPanel.add(toolbar);
    this.getContentPane().add(editorPanel);
  }

  public static void main(String[] args) throws Exception {
    ContourPlotFrame test = new ContourPlotFrame("ContourPlotTest");
    test.pack();
    test.setVisible(true);
  }
}