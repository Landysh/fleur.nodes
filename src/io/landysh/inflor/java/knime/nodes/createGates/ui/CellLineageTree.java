package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.function.Consumer;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.dataStructures.DomainObject;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.subsets.RootSubset;

@SuppressWarnings("serial")
public class CellLineageTree extends JTree {

	/**
	 * Panel which stores and controls the layout of the plots in a given
	 * lineage analysis.
	 */

	private DefaultMutableTreeNode root;
	private TreeCellPlotRenderer renderer;

	public CellLineageTree() {
		
	}

	public void updateLayout(Hashtable<String, ChartSpec> specs, FCSFrame dataStore) {
		super.removeAll();
		//initialize the tree
		renderer = new TreeCellPlotRenderer(dataStore);
		root = new DefaultMutableTreeNode(new RootSubset(dataStore));
		this.setRowHeight(50);
		DefaultTreeModel m_tree = new DefaultTreeModel(root);
		
		if (specs!=null){
			Consumer<ChartSpec> appendToTree = spec -> m_tree.insertNodeInto(
					(MutableTreeNode) new DefaultMutableTreeNode(spec),
					root, 
					root.getChildCount());
			
			specs.values().forEach(appendToTree);

			Consumer<ChartSpec> setHierarchy = spec -> updateHierarchy(m_tree, spec.ID);	
			specs.values().forEach(setHierarchy);
		}	
		
		this.setModel(m_tree);
		this.setCellRenderer(renderer);
		this.setRowHeight(0);
	}

	private Object updateHierarchy(DefaultTreeModel m_tree, String uuid) {
			DefaultMutableTreeNode childNode = findNode(m_tree, uuid);
			if (childNode!=null){
				String parentID = ((ChartSpec) childNode.getUserObject()).getParent();
				DefaultMutableTreeNode parentNode = root;
				if (parentID!=null){
					 parentNode = findNode(m_tree, parentID);
				}
				if (childNode.isNodeChild(parentNode)){
					//Do nothing if it is already a child of it's parent.
				}else if(parentNode==null){
					//Then the parent node is likely the root node.I think.
					m_tree.insertNodeInto(childNode, root, root.getChildCount()-1);
				} else {
					//Otherwise, add it to the desired parent node.
					m_tree.insertNodeInto(childNode, parentNode, parentNode.getChildCount()-1);
				}
			}
		return null;
	}

	private DefaultMutableTreeNode findNode(DefaultTreeModel m_tree, String queryID) {
		/**
		 * Attempts to find a node by ID. Will return null if it fails to find the requested ID. 
		 * @param m_tree - the tree to search.
		 * @param id - the id to search for.
		 */
		Enumeration<?> nodeEnum = root.preorderEnumeration();
		while (nodeEnum.hasMoreElements()){
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
				DomainObject node = (DomainObject) currentNode.getUserObject();
				if (node.ID.equals(queryID)){
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
//EOF