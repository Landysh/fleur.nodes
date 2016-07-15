package io.landysh.inflor.java.knime.nodes.removeDoublets;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.singlets.SingletsModel;
import io.landysh.inflor.java.core.utils.FCSUtils;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortObject;
import io.landysh.inflor.java.knime.portTypes.annotatedVectorStore.ColumnStorePortSpec;

/**
 * This is the model implementation of FindSingletsFrame.
 * 
 *
 * @author Aaron Hart
 */
public class RemoveDoubletsFrameNodeModel extends NodeModel {

	// Area parameter
	static final String CFGKEY_AreaColumn = "Area Column";
	static final String DEFAULT_AreaColumn = null;
	private final SettingsModelString m_AreaColumn = new SettingsModelString(CFGKEY_AreaColumn, DEFAULT_AreaColumn);

	// Height parameter
	static final String CFGKEY_HeightColumn = "Height Column";
	static final String DEFAULT_HeightColumn = null;
	private final SettingsModelString m_HeightColumn = new SettingsModelString(CFGKEY_HeightColumn,
			DEFAULT_HeightColumn);

	/**
	 * Constructor for the node model.
	 */
	protected RemoveDoubletsFrameNodeModel() {

		super(new PortType[] { PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class) },
				new PortType[] { PortTypeRegistry.getInstance().getPortType(ColumnStorePortObject.class) });

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		ColumnStorePortObject inPort = (ColumnStorePortObject) inData[0];
		ColumnStorePortSpec inSpec = (ColumnStorePortSpec) inPort.getSpec();
		ColumnStore inColumnStore = inPort.getColumnStore();
		SingletsModel model = new SingletsModel(inSpec.columnNames);
		double[] area = inColumnStore.getColumn(m_AreaColumn.getStringValue());
		double[] height = inColumnStore.getColumn(m_HeightColumn.getStringValue());
		double[] ratio = model.buildModel(area, height);
		boolean[] mask = model.scoreModel(ratio);

		ColumnStore outStore = new ColumnStore(inColumnStore.getKeywords(), inColumnStore.getColumnNames());

		for (String name : inColumnStore.getColumnNames()) {
			double[] maskedColumn = FCSUtils.getMaskColumn(mask, inColumnStore.getColumn(name));
			outStore.addColumn(name, maskedColumn);
		}

		ColumnStorePortSpec outSpec = new ColumnStorePortSpec(inSpec.keywords, inSpec.columnNames,
				outStore.getRowCount());
		FileStoreFactory fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
		FileStore filestore = fileStoreFactory.createFileStore("column.store");
		ColumnStorePortObject outPort = ColumnStorePortObject.createPortObject(outSpec, outStore, filestore);
		return new PortObject[] { outPort };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		ColumnStorePortSpec inSpec = (ColumnStorePortSpec) inSpecs[0];

		return new ColumnStorePortSpec[] { getSpec(inSpec) };
	}

	private ColumnStorePortSpec getSpec(ColumnStorePortSpec inSpec) {
		ColumnStorePortSpec outSpec = new ColumnStorePortSpec(inSpec.keywords, inSpec.columnNames,
				inSpec.getRowCount());
		return outSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		settings.addString(CFGKEY_AreaColumn, m_AreaColumn.getStringValue());
		settings.addString(CFGKEY_HeightColumn, m_HeightColumn.getStringValue());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_AreaColumn.setStringValue(settings.getString(CFGKEY_AreaColumn));
		m_HeightColumn.setStringValue(settings.getString(CFGKEY_HeightColumn));

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

}
