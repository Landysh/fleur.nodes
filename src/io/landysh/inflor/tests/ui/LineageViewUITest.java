package io.landysh.inflor.tests.ui;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.LogrithmicDisplayTransform;
import io.landysh.inflor.java.knime.nodes.createGates.ui.CellLineageTree;

@SuppressWarnings("serial")
public class LineageViewUITest extends ApplicationFrame {
	   public LineageViewUITest(String title) throws Exception {
		super(title);
		
		Hashtable<String, ChartSpec> testSpecs = new Hashtable<String, ChartSpec>();
		
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		final FCSFileReader reader = new FCSFileReader(logiclePath, false);
		reader.readData();
		final ColumnStore dataStore = reader.getColumnStore();
		
		ChartSpec ly = new ChartSpec();
		ly.setPlotType(PlotTypes.Contour);
		ly.setDomainAxisName("SSC-W");
		ly.setRangeAxisName("SSC-A");
		ly.setxBinCount(256);
		ly.setyBinCount(256);
		ly.setxMin(0);
		ly.setyMin(1000);
		ly.setxMax(262144);
		ly.setyMax(262144);
		
		ChartSpec ly2 = new ChartSpec();
		ly2.setPlotType(PlotTypes.Contour);
		ly2.setDomainAxisName("FSC-A");
		ly2.setRangeAxisName("SSC-A");
		ly2.setRangeTransform(new LogrithmicDisplayTransform(1000, 262144));
		ly2.setxBinCount(1024);
		ly2.setyBinCount(1024);
		ly2.setxMin(0);
		ly2.setyMin(1000);
		ly2.setxMax(262144);
		ly2.setyMax(262144);
		ly2.setParent(ly.UUID);
		
		ChartSpec ly3 = new ChartSpec();
		ly3.setPlotType(PlotTypes.Scatter);
		ly3.setDomainAxisName("FSC-A");
		ly3.setRangeAxisName("SSC-A");
		ly3.setxBinCount(1024);
		ly3.setyBinCount(1024);
		ly3.setxMin(0);
		ly3.setyMin(1000);
		ly3.setxMax(262144);
		ly3.setyMax(262144); 
		ly3.setParent(ly.UUID);

		

		testSpecs.put(ly.UUID, ly);
		testSpecs.put(ly2.UUID, ly2);
		testSpecs.put(ly3.UUID, ly3);

		
		UIDefaults defaults = UIManager.getDefaults();
		Integer oldValue = (int) defaults.get("Tree.leftChildIndent");
		defaults.put("Tree.leftChildIndent" , new Integer(110) );

		CellLineageTree testPanel = new CellLineageTree();
		testPanel.updateLayout(testSpecs, dataStore);
		testPanel.setRootVisible(false);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.getContentPane().add(testPanel);
		defaults.put("Tree.leftChildIndent" , oldValue );
	}

	public static void main(String[] args) throws Exception {
		LineageViewUITest test = new LineageViewUITest("ContourPlotTest");
			  test.pack();
			  test.setSize(new Dimension(400, 700));
		      test.setVisible(true);
	}	   
}
//EOF