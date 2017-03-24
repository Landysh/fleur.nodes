package inflor.knime.nodes.bhtsne.column;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

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
public class TSNEColumnNodeDialog extends NodeDialogPane {

  /**
   * New pane for configuring the TSNE node.
   */
  protected TSNEColumnNodeDialog() {
    super();
    // Column Filter Panel
  }

  @Override
  protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
    
  }
}