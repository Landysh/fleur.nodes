package io.landysh.inflor.tests.ui;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotTypes;
import io.landysh.inflor.java.core.transforms.LogrithmicTransform;
import io.landysh.inflor.java.knime.nodes.createGates.ui.CellLineageTree;

@SuppressWarnings("serial")
public class LineageViewUITest extends ApplicationFrame {
	   public LineageViewUITest(String title) throws Exception {
		super(title);
		
		Hashtable<String, ChartSpec> testSpecs = new Hashtable<String, ChartSpec>();
		
		String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		final FCSFileReader reader = new FCSFileReader(logiclePath);
		reader.readData();
		final FCSFrame dataStore = reader.getColumnStore();
		
		ChartSpec ly = new ChartSpec();
		ly.setPlotType(PlotTypes.Contour);
		ly.setDomainAxisName("SSC-W");
		ly.setRangeAxisName("SSC-A");

		ChartSpec ly2 = new ChartSpec();
		ly2.setPlotType(PlotTypes.Contour);
		ly2.setDomainAxisName("FSC-A");
		ly2.setRangeAxisName("SSC-A");
		ly2.setRangeTransform(new LogrithmicTransform(1000, 262144));

		
		ChartSpec ly3 = new ChartSpec();
		ly3.setPlotType(PlotTypes.Scatter);
		ly3.setDomainAxisName("FSC-A");
		ly3.setRangeAxisName("SSC-A");
		ly3.setParent(ly.ID);

		

		testSpecs.put(ly.ID, ly);
		testSpecs.put(ly2.ID, ly2);
		testSpecs.put(ly3.ID, ly3);

		
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