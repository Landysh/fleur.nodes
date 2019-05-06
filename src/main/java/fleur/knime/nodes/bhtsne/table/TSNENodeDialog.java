package fleur.knime.nodes.bhtsne.table;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "TSNE" Node. Calculates a tSNE using library developed by Leif
 * Jonsson: nhttps://github.com/lejon/T-SNE-Java
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple
 * dialog with standard components. If you need a more complex dialog please derive directly from
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
    // Column Filter Panel
    final SettingsModelColumnFilter2 modelColumns =
        new SettingsModelColumnFilter2(TSNENodeModel.KEY_SLECTED_COLUMNS);
    final DialogComponent diaC = new DialogComponentColumnFilter2(modelColumns, 0);
    addDialogComponent(diaC);

    // Iterations
    final SettingsModelIntegerBounded modelIterations = new SettingsModelIntegerBounded(
        TSNENodeModel.KEY_ITERATIONS, TSNENodeModel.DEFAULT_ITERATIONS,
        TSNENodeModel.MIN_ITERATIONS, TSNENodeModel.MAX_ITERATIONS);
    addDialogComponent(new DialogComponentNumber(modelIterations, "Runtime (Iterations)", 1));

    // Initial PCA Dims
    final SettingsModelIntegerBounded modelInitDims =
        new SettingsModelIntegerBounded(TSNENodeModel.KEY_PCA_DIMS,
            TSNENodeModel.DEFAULT_PCA_DIMS, TSNENodeModel.MIN_PCA_DIMS, TSNENodeModel.MAX_PCA_DIMS);
    addDialogComponent(new DialogComponentNumber(modelInitDims, "Initial Dimensions", 1));

    // Perplexity
    final SettingsModelDoubleBounded modelPerplexity = new SettingsModelDoubleBounded(
        TSNENodeModel.KEY_PERPLEXITY, TSNENodeModel.DEFAULT_PERPLEXITY,
        TSNENodeModel.MIN_PERPLEXITY, TSNENodeModel.MAX_PERPLEXITY);
    addDialogComponent(new DialogComponentNumber(modelPerplexity, "Perplexity", 1));
    
    // Random seed
    final SettingsModelInteger modelSeed =
        new SettingsModelInteger(TSNENodeModel.KEY_SEED,TSNENodeModel.DEFAULT_SEED);
    addDialogComponent(new DialogComponentNumber(modelSeed, TSNENodeModel.KEY_SEED, 1));
  }
}