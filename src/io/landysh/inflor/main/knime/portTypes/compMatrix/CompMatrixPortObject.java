package io.landysh.inflor.main.knime.portTypes.compMatrix;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;

public class CompMatrixPortObject extends AbstractSimplePortObject {
  
  private static final String IN_DIMENSIONS_KEY =   "Input Dimensions";
  private static final String SPILLOVER_VALUE_KEY = "Spillover Values";
  private static final String OUT_DIMENSIONS_KEY =  "Output Dimensions";
  
  String[] m_inputDimensions;
  String[] m_outputDimensions;
  double[] m_spilloverValues;
  
  public CompMatrixPortObject(CompMatrixPortSpec spec, double[] spilloverValues) {
    m_inputDimensions = spec.getinputDimensions();
    m_outputDimensions = spec.getoutputDimensions();
    m_spilloverValues = spilloverValues;
  }

  @Override
  public String getSummary() {
    return String.join(",", m_outputDimensions);
  }

  @Override
  public PortObjectSpec getSpec() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void save(ModelContentWO model, ExecutionMonitor exec)
      throws CanceledExecutionException {
    model.addStringArray(IN_DIMENSIONS_KEY, m_inputDimensions);
    model.addStringArray(OUT_DIMENSIONS_KEY, m_outputDimensions);
    model.addDoubleArray(SPILLOVER_VALUE_KEY, m_spilloverValues);
  }

  @Override
  protected void load(ModelContentRO model, PortObjectSpec spec, ExecutionMonitor exec)
      throws InvalidSettingsException, CanceledExecutionException {
    
    m_inputDimensions = model.getStringArray(IN_DIMENSIONS_KEY);
    m_outputDimensions = model.getStringArray(OUT_DIMENSIONS_KEY);
    m_spilloverValues = model.getDoubleArray(SPILLOVER_VALUE_KEY);
  
  }
}