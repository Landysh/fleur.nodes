package tests.java.inflor.unit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.junit.Test;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.gates.Hierarchical;
import main.java.inflor.core.gates.RangeGate;
import main.java.inflor.core.ui.CellLineageTree;

public class CellLineageTreeTest{

  @Test
  public void test1() throws Exception {
    System.out.println("Running test 1");
    String path = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";

    FCSFrame dataFrame = FCSFileReader.read(path);
    
    String xName = "FSC-H";
    String yName = "FSC-W";
    
    RangeGate g1 = new RangeGate("1", new String[]{xName, yName}, new double[]{0,0}, new double[]{1,1}, "1");
    RangeGate g2 = new RangeGate("2", new String[]{xName, yName}, new double[]{0,0}, new double[]{1,1}, "2");
    RangeGate g3 = new RangeGate("3", new String[]{xName, yName}, new double[]{0,0}, new double[]{1,1}, "3");
    RangeGate g4 = new RangeGate("4", new String[]{xName, yName}, new double[]{0,0}, new double[]{1,1}, "4");
    g3.setParentID(g2.getID());
    g4.setParentID(g3.getID());
    List<Hierarchical> nodePool = new ArrayList<Hierarchical>();
    nodePool.add(g1);
    nodePool.add(g2);
    nodePool.add(g3);
    nodePool.add(g4);
    
    CellLineageTree tree = new CellLineageTree(dataFrame, nodePool);
    DefaultTreeModel model =  (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
    @SuppressWarnings("unchecked")
    Enumeration<DefaultMutableTreeNode> enumeration = rootNode.depthFirstEnumeration();
    int i=0;
    while (enumeration.hasMoreElements()){
      System.out.print(i);
      DefaultMutableTreeNode dmtn = enumeration.nextElement();
      if (dmtn.getUserObject() instanceof Hierarchical){
        Hierarchical node = (Hierarchical) enumeration.nextElement().getUserObject();
        System.out.println(node.getID() + " child of " + node.getParentID());
      }
      i++;
    }
  }
}
