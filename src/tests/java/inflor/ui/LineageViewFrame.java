package tests.java.inflor.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jfree.ui.ApplicationFrame;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.gates.Hierarchical;
import main.java.inflor.core.gates.RectangleGate;
import main.java.inflor.core.plots.ChartSpec;
import main.java.inflor.core.plots.PlotTypes;
import main.java.inflor.core.ui.CellLineageTree;

@SuppressWarnings("serial")
public class LineageViewFrame extends ApplicationFrame {
  public LineageViewFrame(String title) throws Exception {
    super(title);

    List<Hierarchical> testSpecs = new ArrayList<Hierarchical>();

    String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
    final FCSFileReader reader = new FCSFileReader(logiclePath);
    reader.readData();
    final FCSFrame dataStore = reader.getFCSFrame();

    ChartSpec ly = new ChartSpec();
    ly.setPlotType(PlotTypes.DENSITY);
    ly.setDomainAxisName("SSC-W");
    ly.setRangeAxisName("SSC-A");

    ChartSpec ly2 = new ChartSpec();
    ly2.setPlotType(PlotTypes.DENSITY);
    ly2.setDomainAxisName("FSC-A");
    ly2.setRangeAxisName("SSC-A");


    ChartSpec ly3 = new ChartSpec();
    ly3.setPlotType(PlotTypes.DENSITY);
    ly3.setDomainAxisName("FSC-A");
    ly3.setRangeAxisName("SSC-A");
    ly3.setParentID(ly.getID());
    
    RectangleGate g1 = new RectangleGate("LY", "FSC-A", 25000, 100000, "SSC-A", 36000, 200000, "gate");
    g1.setParentID(dataStore.getID());
    ly.setParentID("gate");

    testSpecs.add(ly);
    testSpecs.add(ly2);
    testSpecs.add(ly3);
    testSpecs.add(g1);


    UIDefaults defaults = UIManager.getDefaults();
    Integer oldValue = (int) defaults.get("Tree.leftChildIndent");
    defaults.put("Tree.leftChildIndent", new Integer(110));

    CellLineageTree testPanel = new CellLineageTree(dataStore,testSpecs);
    testPanel.setRootVisible(true);

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.getContentPane().add(testPanel);
    defaults.put("Tree.leftChildIndent", oldValue);
  }

  public static void main(String[] args) throws Exception {
    LineageViewFrame test = new LineageViewFrame("ContourPlotTest");
    test.pack();
    test.setSize(new Dimension(400, 700));
    test.setVisible(true);
  }
}
// EOF