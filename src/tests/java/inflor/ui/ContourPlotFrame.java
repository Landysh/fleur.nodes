package inflor.ui;

import java.util.BitSet;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import inflor.core.data.FCSFrame;
import inflor.core.downsample.DownSample;
import inflor.core.fcs.FCSFileReader;
import inflor.core.gates.ui.GateCreationToolBar;
import inflor.core.plots.ChartSpec;
import inflor.core.plots.DensityPlot;
import inflor.core.plots.FCSChartPanel;
import inflor.core.plots.PlotTypes;
import inflor.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class ContourPlotFrame extends ApplicationFrame {

  private FCSChartPanel panel;
  transient MouseInputListener activeListener;
  private GateCreationToolBar toolbar;

  public ContourPlotFrame(String title) {
    super(title);
    // Setup data
    String path = "C:\\Users\\Aaron\\Documents\\GitHub\\Inflor\\Inflor\\src\\tests\\resources\\fcs\\logicle-example.fcs";
    FCSFrame fullFrame = FCSFileReader.read(path);
    BitSet ddds = DownSample.densityDependent(fullFrame, fullFrame.getDimensionNames(), 2000);
    FCSFrame dddsFrame = FCSUtilities.filterFrame(ddds, fullFrame);
    
    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.DENSITY);
    spec.setDomainAxisName("FSC-A");
    spec.setRangeAxisName("SSC-A");

    DensityPlot plot = new DensityPlot(spec);
    JFreeChart chart = plot.createChart(dddsFrame);
    panel = new FCSChartPanel(chart, spec, dddsFrame);
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