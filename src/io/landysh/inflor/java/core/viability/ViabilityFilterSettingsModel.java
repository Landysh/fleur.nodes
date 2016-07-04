package io.landysh.inflor.java.core.viability;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class ViabilityFilterSettingsModel {

	//Viability parameter
	static final String CFGKEY_ViabilityColumn = "Area Column";
	static final String DEFAULT_ViabilityColumn = null;
	private final SettingsModelString m_ViabilityColumn = new SettingsModelString(
			CFGKEY_ViabilityColumn,
			DEFAULT_ViabilityColumn);
	
	//Selected column from input table upon which to do the discrimination.
	static  final String CFGKEY_FCSColumn = "FCS Column";
	static  final String DEFAULT_FCSColumn = null;
	private final SettingsModelString m_FCSColumn= new SettingsModelString(
			CFGKEY_FCSColumn,
			DEFAULT_FCSColumn);	
	
	public void saveSettingsTo(NodeSettingsWO settings) {
		m_FCSColumn.saveSettingsTo(settings);
		m_ViabilityColumn.saveSettingsTo(settings);		
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		m_FCSColumn.loadSettingsFrom(settings);
		m_ViabilityColumn.loadSettingsFrom(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_FCSColumn.validateSettings(settings);
		m_ViabilityColumn.validateSettings(settings);
	}
	
	public SettingsModelString getSelectedColumnSettingsModel() 	{return m_FCSColumn;}
	public SettingsModelString getViabilityColumnSettingsModel() 	{return m_ViabilityColumn;}
}
