package io.landysh.inflor.tests.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.ui.CellLineageTree;

@SuppressWarnings("serial")
public class LineageViewUITest extends ApplicationFrame {
  public LineageViewUITest(String title) throws Exception {
    super(title);

    HashMap<String, ChartSpec> testSpecs = new HashMap<String, ChartSpec>();

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



    testSpecs.put(ly.getID(), ly);
    testSpecs.put(ly2.getID(), ly2);
    testSpecs.put(ly3.getID(), ly3);


    UIDefaults defaults = UIManager.getDefaults();
    Integer oldValue = (int) defaults.get("Tree.leftChildIndent");
    defaults.put("Tree.leftChildIndent", new Integer(110));

    CellLineageTree testPanel = new CellLineageTree();
    testPanel.updateLayout(testSpecs.values(), new ArrayList<AbstractGate>(), dataStore);
    testPanel.setRootVisible(false);

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
