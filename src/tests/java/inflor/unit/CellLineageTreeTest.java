package tests.java.inflor.unit;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import io.landysh.inflor.main.core.gates.Hierarchical;
import io.landysh.inflor.main.core.gates.RectangleGate;
import io.landysh.inflor.main.core.ui.CellLineageTree;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;

public class CellLineageTreeTest{

  public static void main(String[] args) throws Exception {
    CellLineageTreeTest.run();
  }

  private static void run() {
    System.out.println("Running test 1");
    String path = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";

    FCSFrame dataFrame = FCSFileReader.read(path);
    
    String xName = "FSC-H";
    String yName = "FSC-W";
    
    RectangleGate g1 = new RectangleGate("1", xName, 0, 1, yName, 0, 1, "1");
    RectangleGate g2 = new RectangleGate("2", xName, 0, 1, yName, 0, 1, "2");
    RectangleGate g3 = new RectangleGate("3", xName, 0, 1, yName, 0, 1, "3");
    RectangleGate g4 = new RectangleGate("4", xName, 0, 1, yName, 0, 1, "4");
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
