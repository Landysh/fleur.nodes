package io.landysh.inflor.java.knime.nodes.calculateTSNE;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "TSNE" Node.
 * Calculates a tSNE using library developed by Leif Jonsson: * nhttps://github.com/lejon/T-SNE-Java
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Aaron Hart
 */
public class TSNENodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the TSNE node.
     */
    protected TSNENodeDialog() {
		super();
    	//Column Filter Panel
		SettingsModelColumnFilter2 modelColumns = new SettingsModelColumnFilter2(TSNENodeModel.CFGKEY_Columns);
		DialogComponent diaC = new DialogComponentColumnFilter2(modelColumns, 0);
		addDialogComponent(diaC);
		
		//Iterations
		SettingsModelIntegerBounded modelIterations = 
				new SettingsModelIntegerBounded(TSNENodeModel.CFGKEY_Iterations, 
				TSNENodeModel.DEFAULT_Iterations, 
				TSNENodeModel.MIN_Iterations, 
				TSNENodeModel.MAX_Iterations);
		addDialogComponent(new DialogComponentNumber(modelIterations,"Runtime (Iterations)", 1));
		
		
		//Initial PCA Dims
		SettingsModelIntegerBounded modelInitDims = 
				new SettingsModelIntegerBounded(TSNENodeModel.CFGKEY_InitDims, 
				TSNENodeModel.DEFAULT_InitDims, 
				TSNENodeModel.MIN_InitDims, 
				TSNENodeModel.MAX_InitDims);
		addDialogComponent(new DialogComponentNumber(modelInitDims,"Initial Dimensions", 1));
		
		

		//Perplexity
		SettingsModelDoubleBounded modelPerplexity = new SettingsModelDoubleBounded(TSNENodeModel.CFGKEY_Perplexity, 
				TSNENodeModel.DEFAULT_Perplexity, 
				TSNENodeModel.MIN_Perplexity, 
				TSNENodeModel.MAX_Perplexity);
		addDialogComponent(new DialogComponentNumber(modelPerplexity,"Perplexity", 1));
    }

}