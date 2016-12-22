package tests.java.inflor.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.gates.ui.GateCreationToolBar;
import main.java.inflor.core.plots.ChartSpec;
import main.java.inflor.core.plots.DensityPlot;
import main.java.inflor.core.plots.FCSChartPanel;
import main.java.inflor.core.plots.PlotTypes;
import main.java.inflor.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class ContourPlotTest_UI extends ApplicationFrame {

  private FCSChartPanel panel;
  MouseInputListener activeListener;
  private GateCreationToolBar toolbar;

  public ContourPlotTest_UI(String title) throws Exception {
    super(title);
    // Setup data
    String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
    //String bigPath = "src/io/landysh/inflor/tests/extData/20mbFCS3.fcs";
    final FCSFrame dataStore = FCSFileReader.read(logiclePath);

    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.DENSITY);
    spec.setDomainAxisName("FSC-A");
    spec.setRangeAxisName("SSC-A");
    FCSDimension X = FCSUtilities.findCompatibleDimension(dataStore, spec.getDomainAxisName());
    FCSDimension Y = FCSUtilities.findCompatibleDimension(dataStore, spec.getRangeAxisName());

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
    ContourPlotTest_UI test = new ContourPlotTest_UI("ContourPlotTest");
    test.pack();
    test.setVisible(true);
  }
}
