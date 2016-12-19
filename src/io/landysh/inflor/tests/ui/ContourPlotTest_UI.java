package io.landysh.inflor.tests.ui;


import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.main.core.data.FCSDimension;
import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.gates.ui.GateCreationToolBar;
import io.landysh.inflor.main.core.plots.ChartSpec;
import io.landysh.inflor.main.core.plots.DensityPlot;
import io.landysh.inflor.main.core.plots.FCSChartPanel;
import io.landysh.inflor.main.core.plots.PlotTypes;
import io.landysh.inflor.main.core.utils.FCSUtilities;

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
    spec.setPlotType(PlotTypes.Density);
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
