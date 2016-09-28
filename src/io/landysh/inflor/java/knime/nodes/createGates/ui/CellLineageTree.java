package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.function.Consumer;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.plots.ChartSpec;

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

	public void updateLayout(Hashtable<String, ChartSpec> specs, ColumnStore dataStore) {
		super.removeAll();
		//initialize the tree
		renderer = new TreeCellPlotRenderer(dataStore);
		root = new DefaultMutableTreeNode("Root");
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
		//this.setEditable(false);
		this.setRowHeight(150);
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
					//Do nothing
				} else {
					m_tree.insertNodeInto(childNode, parentNode, 0);
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
			if(currentNode.getUserObject().getClass().equals(ChartSpec.class)){
				ChartSpec currentSpec = (ChartSpec) currentNode.getUserObject();
				if (currentSpec.ID.equals(queryID)){
					return currentNode;
				}
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
        setRootVisible(false);
        setShowsRootHandles(false);
      }
}
//EOF