package io.landysh.inflor.tests.ui;

import java.util.Hashtable;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.jfree.ui.ApplicationFrame;

import io.landysh.inflor.java.core.plots.PlotSpec;
import io.landysh.inflor.java.knime.nodes.createGates.ui.LineageAnalysisPanel;

@SuppressWarnings("serial")
public class LineageViewUITest extends ApplicationFrame {
	   public LineageViewUITest(String title) throws Exception {
		super(title);
		
		Hashtable<String, PlotSpec> testSpecs = new Hashtable<String, PlotSpec>();
		
		PlotSpec spec1 = new PlotSpec();
		PlotSpec spec2 = new PlotSpec();
		PlotSpec spec3 = new PlotSpec();
		
		spec2.setParent(spec1.getUUID());

		testSpecs.put(spec1.uuid, spec1);
		testSpecs.put(spec2.uuid, spec2);
		testSpecs.put(spec3.uuid, spec3);
		
		
		//
		String s1 = "Foo";
		String s2 = "FooBar";
		String s3 = "Bar";
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("patoot");

		DefaultMutableTreeNode n1 = new DefaultMutableTreeNode(s1);
		DefaultMutableTreeNode n2 = new DefaultMutableTreeNode(s2);
		DefaultMutableTreeNode n3 = new DefaultMutableTreeNode(s3);
		
		DefaultTreeModel model = new DefaultTreeModel(root);
		model.insertNodeInto(n1, root, 0);
		model.insertNodeInto(n3, root, 1);
		model.insertNodeInto(n2, n1, 0);

		
		
		JTree tree = new JTree(model);
		tree.setRootVisible(false);

		
		

		LineageAnalysisPanel testPanel = new LineageAnalysisPanel(testSpecs);
		testPanel.updateLayout();
		testPanel.setRootVisible(true);

		this.getContentPane().add(tree);
	}

	public static void main(String[] args) throws Exception {
		LineageViewUITest test = new LineageViewUITest("ContourPlotTest");
			  test.pack();
		      test.setVisible(true);
	}	   
}
//EOF