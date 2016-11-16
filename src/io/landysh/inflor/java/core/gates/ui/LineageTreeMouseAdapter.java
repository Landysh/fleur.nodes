package io.landysh.inflor.java.core.gates.ui;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.gates.GateUtilities;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.ui.CellLineageTree;
import io.landysh.inflor.java.core.utils.FCSUtilities;
import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;
import io.landysh.inflor.java.knime.nodes.createGates.ui.ChartEditorDialog;
import sun.awt.windows.WEmbeddedFrame;

public class LineageTreeMouseAdapter extends MouseInputAdapter {
  private CreateGatesNodeDialog parent;

  public LineageTreeMouseAdapter(CreateGatesNodeDialog parentDialog){
    this.parent = parentDialog;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    CellLineageTree tree = parent.lineageTree;
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
      //Extract some needed objects from the tree.
      TreePath treePath = tree.getAnchorSelectionPath();    
      Object[] elements = treePath.getPath();
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)elements[elements.length-1];
      Object selectedObject = selectedNode.getUserObject();
      //Pull out the dataFrame from the root node.
      FCSFrame dataFrame = (FCSFrame)((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject();
      //Extract gates from tree object path. 
      Supplier<ArrayList<AbstractGate>> supplier = () -> new ArrayList<AbstractGate>();
      ArrayList<AbstractGate> parentGates = Arrays.asList(elements)
          .stream()
          .filter(pathObject -> pathObject instanceof DefaultMutableTreeNode)
          .map(pathObject -> ((DefaultMutableTreeNode) pathObject).getUserObject())
          .filter(nodeObject -> nodeObject instanceof AbstractGate)
          .map(nodeObject -> (AbstractGate) nodeObject)
          .collect(Collectors.toCollection(supplier));
      //Calculate the path mask and apply to dataFrame to create a filtered frame.
      BitSet mask = GateUtilities.applyGatingPath(dataFrame, parentGates);
      FCSFrame filteredFrame = FCSUtilities.filterColumnStore(mask, dataFrame);
      if (parentGates.size()>=1){
        String ids = parentGates.get(parentGates.size()-1).getID();
        filteredFrame.setID(ids);
      }
      WEmbeddedFrame topFrame = (WEmbeddedFrame) SwingUtilities.getWindowAncestor(this.parent.getPanel());
      ArrayList<AbstractGate> siblingGates = new ArrayList<>();
      
      if (selectedNode.getSiblingCount()!=1){
        for (int i=0;i<selectedNode.getSiblingCount();i++){
          DefaultMutableTreeNode currentSibling = (DefaultMutableTreeNode) selectedNode.getParent().getChildAt(i);
          if (currentSibling!=selectedNode&&currentSibling.getUserObject() instanceof AbstractGate){
            siblingGates.add((AbstractGate) currentSibling.getUserObject());
          }
        }
      }
      
      if(selectedObject instanceof AbstractGate){
        ChartEditorDialog dialog = new ChartEditorDialog(topFrame, filteredFrame, siblingGates);
        popDialog(dialog);
      } else if (selectedObject instanceof ChartSpec){
        ChartSpec spec = (ChartSpec) selectedObject;
        ChartEditorDialog dialog = new ChartEditorDialog(topFrame, filteredFrame, siblingGates, spec);
        popDialog(dialog);
      } else if (selectedObject instanceof FCSFrame){
        ChartEditorDialog dialog = new ChartEditorDialog(topFrame, dataFrame, siblingGates);
        popDialog(dialog);
      }
    }   
  }

  private void popDialog(ChartEditorDialog dialog) {
    dialog.setVisible(true);
    if (dialog.isOK) {
      parent.m_settings.addNode(dialog.getChartSpec());
      List<AbstractGate> gates = dialog.getGates();
      gates.forEach(gate -> parent.m_settings.addNode(gate));
      parent.updateLineageTree();
    }
    dialog.dispose();
  }
}
