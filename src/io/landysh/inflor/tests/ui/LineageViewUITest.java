package io.landysh.inflor.tests.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.gates.Hierarchical;
import io.landysh.inflor.main.core.gates.RectangleGate;
import io.landysh.inflor.main.core.plots.ChartSpec;
import io.landysh.inflor.main.core.plots.PlotTypes;
import io.landysh.inflor.main.core.ui.CellLineageTree;

@SuppressWarnings("serial")
public class LineageViewUITest extends ApplicationFrame {
  public LineageViewUITest(String title) throws Exception {
    super(title);

    List<Hierarchical> testSpecs = new ArrayList<Hierarchical>();

    String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
    final FCSFileReader reader = new FCSFileReader(logiclePath);
    reader.readData();
    final FCSFrame dataStore = reader.getColumnStore();

    ChartSpec ly = new ChartSpec();
    ly.setPlotType(PlotTypes.Density);
    ly.setDomainAxisName("SSC-W");
    ly.setRangeAxisName("SSC-A");

    ChartSpec ly2 = new ChartSpec();
    ly2.setPlotType(PlotTypes.Density);
    ly2.setDomainAxisName("FSC-A");
    ly2.setRangeAxisName("SSC-A");


    ChartSpec ly3 = new ChartSpec();
    ly3.setPlotType(PlotTypes.Density);
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
    LineageViewUITest test = new LineageViewUITest("ContourPlotTest");
    test.pack();
    test.setSize(new Dimension(400, 700));
    test.setVisible(true);
  }
}
// EOF
