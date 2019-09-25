package inflor.ui;

import java.util.BitSet;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

import fleur.core.data.FCSFrame;
import fleur.core.fcs.FCSFileReader;
import fleur.core.gates.ui.GateCreationToolBar;
import fleur.core.plots.ChartSpec;
import fleur.core.plots.DensityPlot;
import fleur.core.plots.FCSChartPanel;
import fleur.core.plots.PlotTypes;
import fleur.core.sample.DownSample;
import fleur.core.transforms.TransformSet;
import fleur.core.utils.FCSUtilities;

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
    TransformSet transforms = new TransformSet();
	BitSet ddds = DownSample.densityDependent(fullFrame, fullFrame.getDimensionNames(), 2000, transforms);
    FCSFrame dddsFrame = FCSUtilities.filterFrame(ddds, fullFrame);
    
    ChartSpec spec = new ChartSpec();
    spec.setPlotType(PlotTypes.DENSITY);
    spec.setDomainAxisName("FSC-A");
    spec.setRangeAxisName("SSC-A");

    DensityPlot plot = new DensityPlot(spec);
    JFreeChart chart = plot.createChart(dddsFrame, new TransformSet());
    panel = new FCSChartPanel(chart, spec, dddsFrame, new TransformSet());
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