package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.gates.AbstractGate;
import io.landysh.inflor.java.core.plots.ChartSpec;

@SuppressWarnings("serial")
public class CellLineageTree extends JTree {

  /**
   * Panel which stores and controls the layout of the plots in a given lineage analysis.
   */

  private DefaultMutableTreeNode root;
  private TreeCellPlotRenderer renderer;

  public CellLineageTree() {}

  public void updateLayout(Collection<ChartSpec> chartSpecs, Collection<AbstractGate> gates, FCSFrame dataStore) {
    super.removeAll();
    // initialize the tree
    renderer = new TreeCellPlotRenderer();
    root = new DefaultMutableTreeNode(dataStore);
    this.setRowHeight(50);
    DefaultTreeModel m_tree = new DefaultTreeModel(root);
    if (gates!=null){
      gates.forEach(gate -> m_tree.insertNodeInto(new DefaultMutableTreeNode(gate), root, root.getChildCount()));
      gates.forEach(gate -> updateHierarchy(m_tree, gate.ID));
    }
    
    
    if (chartSpecs != null) {
      chartSpecs.forEach(spec -> m_tree.insertNodeInto(new DefaultMutableTreeNode(spec), root,root.getChildCount()));
      chartSpecs.forEach(spec -> updateHierarchy(m_tree, spec.ID));
    }

    this.setModel(m_tree);
    this.setCellRenderer(renderer);
    this.setRowHeight(0);
  }

  private void updateHierarchy(DefaultTreeModel m_tree, String uuid) {
    DefaultMutableTreeNode childNode = findNode(m_tree, uuid);
    if (childNode != null) {
      String parentID = ((ChartSpec) childNode.getUserObject()).getParent();
      DefaultMutableTreeNode parentNode = root;
      if (parentID != null) {
        parentNode = findNode(m_tree, parentID);
      }
      if (childNode.isNodeChild(parentNode)) {
        // Do nothing if it is already a child of it's parent.
      } else if (parentNode == null) {
        // Then the parent node is likely the root node.
        m_tree.insertNodeInto(childNode, root, root.getChildCount() - 1);
      } else {
        // Otherwise, add it to the desired parent node.
        m_tree.insertNodeInto(childNode, parentNode, parentNode.getChildCount() - 1);
      }
    }
  }

  private DefaultMutableTreeNode findNode(DefaultTreeModel m_tree, String queryID) {
    /**
     * Attempts to find a node by ID. Will return null if it fails to find the requested ID.
     * 
     * @param m_tree - the tree to search.
     * @param id - the id to search for.
     */
    Enumeration<?> nodeEnum = root.preorderEnumeration();
    while (nodeEnum.hasMoreElements()) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
      DomainObject node = (DomainObject) currentNode.getUserObject();
      if (node.ID.equals(queryID)) {
        return currentNode;
      }
    }
    return null;
  }

  @Override
  public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    setCellRenderer(renderer);
    setRowHeight(0);
    setRootVisible(true);
    setShowsRootHandles(true);
  }
}
