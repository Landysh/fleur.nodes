/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fleur.core.data.DomainObject;
import fleur.core.data.FCSFrame;
import fleur.core.transforms.TransformSet;
import inflor.core.gates.GateUtilities;
import inflor.core.gates.Hierarchical;

@SuppressWarnings("serial")
public class CellLineageTree extends JTree {

  /**
   * Panel which stores and controls the layout of the plots in a given lineage analysis.
   */

  private DefaultMutableTreeNode root;
  private FCSFrame rootFrame;
  private DefaultMutableTreeNode currentNode;//TODO: scope issue on stream.  maybe a dirty hack.
  private TransformSet transforms;

  
  public CellLineageTree(FCSFrame rootFrame, Collection<Hierarchical> collection, TransformSet transforms) {
    TreeCellPlotRenderer renderer = new TreeCellPlotRenderer(rootFrame, transforms);
    this.setCellRenderer(renderer);
    this.rootFrame = rootFrame;
    this.transforms = transforms;
    this.root = new DefaultMutableTreeNode(GateUtilities.UNGATED_SUBSET_ID);
    this.setModel(buildTree(root, collection));
    for (int i = 0; i < this.getRowCount(); i++) {
      this.expandRow(i);
    }
  }

  @Override
  public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    this.setCellRenderer(new TreeCellPlotRenderer(rootFrame, transforms));
    setRowHeight(0);
    setRootVisible(true);
    setShowsRootHandles(true);
  }
  
  private DefaultTreeModel buildTree(DefaultMutableTreeNode root, Collection<Hierarchical> collection){
    List<Hierarchical> nodePool = collection.stream().collect(Collectors.toList());
    DefaultTreeModel tree = new DefaultTreeModel(root);
    currentNode = root;

    List<DefaultMutableTreeNode> nodesToCheck = new ArrayList<>();
    nodesToCheck.add(root);
    while (!nodesToCheck.isEmpty()){
      List<DefaultMutableTreeNode> dmtNodes = new ArrayList<>(); 
      Object uo = currentNode.getUserObject();
      if (uo instanceof String && uo.equals(GateUtilities.UNGATED_SUBSET_ID)){
        for (Hierarchical node: nodePool){
          //Catch the case where parent is ungated.
          if (node.getParentID().equals(uo)){
            DefaultMutableTreeNode dmtNode = new DefaultMutableTreeNode(node);
            dmtNodes.add(dmtNode);
            nodesToCheck.add(dmtNode);
          } 
        }
      } else {
        String parentID = ((DomainObject) uo).getID();
        for (Hierarchical node: nodePool){
          DefaultMutableTreeNode dmtNode = new DefaultMutableTreeNode(node);
          if (node.getParentID().equals(parentID)){
            dmtNodes.add(dmtNode);
            nodesToCheck.add(dmtNode);
          }
        }
      }
      dmtNodes.stream().filter(dmt ->!dmt.equals(root)).forEach(child -> tree.insertNodeInto(child, currentNode, currentNode.getChildCount()));
      nodesToCheck.remove(currentNode);
      if (!nodesToCheck.isEmpty()){
        currentNode = nodesToCheck.get(0);
      }
    }
    return tree;
  }
}