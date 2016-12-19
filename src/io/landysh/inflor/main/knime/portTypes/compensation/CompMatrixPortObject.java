package io.landysh.inflor.main.knime.portTypes.compensation;

import java.util.Arrays;

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
  
  String[] mInputDimensions;
  String[] mOutputDimensions;
  double[] mSpilloverValues;
  
  public CompMatrixPortObject(CompMatrixPortSpec spec, double[] spilloverValues) {
    mInputDimensions = spec.getInputDimensions();
    mOutputDimensions = spec.getoutputDimensions();
    mSpilloverValues = spilloverValues;
  }

  @Override
  public String getSummary() {
    return String.join(",", mOutputDimensions);
  }

  @Override
  public PortObjectSpec getSpec() {
    return new CompMatrixPortSpec(mInputDimensions, mOutputDimensions);
  }

  @Override
  protected void save(ModelContentWO model, ExecutionMonitor exec)
      throws CanceledExecutionException {
    model.addStringArray(IN_DIMENSIONS_KEY, mInputDimensions);
    model.addStringArray(OUT_DIMENSIONS_KEY, mOutputDimensions);
    model.addDoubleArray(SPILLOVER_VALUE_KEY, mSpilloverValues);
  }
  
  @Override
  protected void load(ModelContentRO model, PortObjectSpec spec, ExecutionMonitor exec)
      throws InvalidSettingsException, CanceledExecutionException {
    
    mInputDimensions = model.getStringArray(IN_DIMENSIONS_KEY);
    mOutputDimensions = model.getStringArray(OUT_DIMENSIONS_KEY);
    mSpilloverValues = model.getDoubleArray(SPILLOVER_VALUE_KEY);
  
  }

  @Override
  public boolean equals(Object o){
    boolean equals = true;
    if (! (o instanceof CompMatrixPortObject)){
      equals = false;
    } else {
      CompMatrixPortObject cmpo = (CompMatrixPortObject) o;
      if (!Arrays.equals(cmpo.getSpilloverValues(), getSpilloverValues())
          ||!Arrays.equals(cmpo.mInputDimensions, mInputDimensions)
          ||!Arrays.equals(cmpo.mOutputDimensions, mOutputDimensions)){
        equals = false;
      } 
    }
    return equals;
  }
  
  @Override
  public int hashCode(){
    int hash = 1;
    hash = hash * Arrays.hashCode(mInputDimensions);
    hash = hash * Arrays.hashCode(mOutputDimensions);
    hash = hash * Arrays.hashCode(mSpilloverValues);
    return hash;
  }
  
  public Double[] getSpilloverValues() {
    Double[] bigD = new Double[mSpilloverValues.length];
    for (int i=0;i<bigD.length;i++){
      bigD[i] = mSpilloverValues[i];
      }
    return bigD;
  }
  
}