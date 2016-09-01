package io.landysh.inflor.java.knime.nodes.BHTSNE;

import java.util.Arrays;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.port.PortObjectSpec;

import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;

/**
 * <code>NodeDialog</code> for the "BHTSNE" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Landysh Co.
 */
public class BHTSNENodeDialog extends DefaultNodeSettingsPane {

	BHTSNESettingsModel m_settings = new BHTSNESettingsModel();

	DialogComponentStringListSelection featuresComponent;
	DialogComponentNumber perplexityComponent;
	DialogComponentNumber iterationsComponent;

	protected BHTSNENodeDialog() {
		super();

		// Iterations
		iterationsComponent = new DialogComponentNumber(m_settings.getIterationsModel(),
				BHTSNESettingsModel.CFGKEY_Iterations, 10);
		addDialogComponent(iterationsComponent);

		// Perplexity
		perplexityComponent = new DialogComponentNumber(m_settings.getPerplexityModel(),
				BHTSNESettingsModel.CFGKEY_Perplexity, 1);
		addDialogComponent(perplexityComponent);

		// Features
		featuresComponent = new DialogComponentStringListSelection(m_settings.getFeaturesModel(),
				BHTSNESettingsModel.CFGKEY_Features, Arrays.asList(m_settings.getFeatures()), true, 25);
		addDialogComponent(featuresComponent);

	}

	/** {@inheritDoc} */
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		final ColumnStorePortSpec spec = (ColumnStorePortSpec) specs[0];
		final String[] vectorNames = spec.columnNames;
		m_settings.getFeaturesModel().setStringArrayValue(vectorNames);
		featuresComponent.replaceListItems(Arrays.asList(vectorNames), vectorNames[0]);
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) {
		// #isthisbullshit?
		m_settings.save(settings);
	}

}