/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.knime.nodes.gating;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import fleur.core.data.FCSFrame;
import fleur.core.transforms.TransformSet;
import inflor.core.gates.AbstractGate;
import inflor.core.gates.GateUtilities;
import inflor.core.plots.ChartSpec;
import inflor.core.ui.CellLineageTree;
import inflor.core.ui.ChartEditorDialog;
import inflor.core.utils.FCSUtilities;

public class LineageTreeMouseAdapter extends MouseInputAdapter {
  private CreateGatesNodeDialog parent;

  public LineageTreeMouseAdapter(CreateGatesNodeDialog parentDialog) {
    this.parent = parentDialog;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    CellLineageTree tree = parent.getTree();
    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
      // Extract some needed objects from the tree.
      TreePath treePath = tree.getAnchorSelectionPath();
      Object[] elements = treePath.getPath();
      DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) elements[elements.length - 1];
      Object selectedObject = selectedNode.getUserObject();
      // Pull out the dataFrame from the root node.
      FCSFrame dataFrame = parent.getCurrentData();
      // Extract gates from tree object path.
      ArrayList<AbstractGate> parentGates = Arrays.asList(elements).stream()
          .filter(pathObject -> pathObject instanceof DefaultMutableTreeNode)
          .map(pathObject -> ((DefaultMutableTreeNode) pathObject).getUserObject())
          .filter(nodeObject -> nodeObject instanceof AbstractGate)
          .map(nodeObject -> (AbstractGate) nodeObject)
          .collect(Collectors.toCollection(ArrayList::new));

      // Calculate the path mask and apply to dataFrame to create a filtered frame.
      BitSet mask = GateUtilities.applyGatingPath(dataFrame, parentGates, new TransformSet());
      FCSFrame filteredFrame;
      if (parentGates.isEmpty()) {
        filteredFrame = dataFrame.deepCopy();
        filteredFrame.setID(GateUtilities.UNGATED_SUBSET_ID);
      } else {
        filteredFrame = FCSUtilities.filterFrame(mask, dataFrame);
        String parentID = parentGates.get(parentGates.size() - 1).getID();
        filteredFrame.setID(parentID);
      }
      Window topFrame = SwingUtilities.getWindowAncestor(this.parent.getPanel());
      ArrayList<AbstractGate> siblingGates = findSiblingGates(selectedNode);
      // Calculate the gating path leading to this dialog.
      List<String> pathElements =
          Arrays.asList(elements).stream().sequential().map(o -> (DefaultMutableTreeNode) o)
              .filter(dmt -> !(dmt.getUserObject() instanceof ChartSpec))
              .map(dmt -> dmt.getUserObject().toString()).collect(Collectors.toList());
      String path = String.join(File.pathSeparator, pathElements);
      // Open the dialog using the existing chart spec if it was clicked on.
      if (selectedObject instanceof ChartSpec) {
        ChartSpec spec = (ChartSpec) selectedObject;
        ChartEditorDialog dialog =
            new ChartEditorDialog(topFrame, path, filteredFrame, siblingGates, parent.getTransforms(), spec);
        popDialog(dialog);
      } else {
        ChartEditorDialog dialog =
            new ChartEditorDialog(topFrame, path, filteredFrame, siblingGates, parent.getTransforms());
        popDialog(dialog);
      }
    }
  }

  private ArrayList<AbstractGate> findSiblingGates(DefaultMutableTreeNode selectedNode) {
    ArrayList<AbstractGate> siblingGates = new ArrayList<>();

    if (selectedNode.getSiblingCount() != 1) {
      for (int i = 0; i < selectedNode.getSiblingCount(); i++) {
        DefaultMutableTreeNode currentSibling =
            (DefaultMutableTreeNode) selectedNode.getParent().getChildAt(i);
        if (currentSibling != selectedNode
            && currentSibling.getUserObject() instanceof AbstractGate) {
          siblingGates.add((AbstractGate) currentSibling.getUserObject());
        }
      }
    }
    return siblingGates;
  }

  private void popDialog(ChartEditorDialog dialog) {
    dialog.setVisible(true);
    if (dialog.isOK()) {
      ChartSpec newSpec = dialog.getChartSpec();
      String specPath =
          String.join(File.pathSeparator, new String[] {dialog.getPath(), newSpec.toString()});
      parent.getSettings().addNode(specPath, newSpec);
      List<AbstractGate> gates = dialog.getGates();
      gates.forEach(gate -> parent.getSettings().addNode(
          String.join(File.pathSeparator, new String[] {dialog.getPath(), gate.toString()}), gate));
      parent.updateLineageTree();
    }
    dialog.dispose();
  }
}
