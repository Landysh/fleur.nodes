package io.landysh.inflor.main.core.compensation;

import static org.ejml.ops.CommonOps.invert;
import static org.ejml.ops.CommonOps.mult;

import java.util.HashMap;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import com.google.protobuf.InvalidProtocolBufferException;

import io.landysh.inflor.main.core.dataStructures.DomainObject;
import io.landysh.inflor.main.core.dataStructures.FCSDimension;
import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class SpilloverCompensator extends DomainObject {

  // Compensation details
  public String[] compParameters;
  private Integer[] compParameterMap;
  private DenseMatrix64F compMatrix;
  private double[][] rawMatrix;

  public SpilloverCompensator(HashMap<String, String> keywords) {
    this(keywords, null);
  }

  public SpilloverCompensator(HashMap<String, String> keywords, String priorUUID) {
    super(priorUUID);
    if (keywords.containsKey("$SPILL") || keywords.containsKey("SPILL")) {
      rawMatrix = parseSpillover(keywords);
      compMatrix = new DenseMatrix64F(rawMatrix);
      invert(compMatrix);
    }
    validate();
  }

  private void validate() {
    if (compParameters.length <= 1) {
      throw new RuntimeException("Invalid compensation matrix");
    }
  }

  // TODO: Unit test
  public double[] compensateRow(double[] FCSRow) {
    double[] compedRow = new double[compParameters.length];
    final double[] unCompedRow = new double[compParameters.length];
    for (int i = 0; i < compParameters.length; i++) {
      final int index = compParameterMap[i];
      unCompedRow[i] = FCSRow[index];
    }
    final DenseMatrix64F unCompedVector = new DenseMatrix64F(new double[][] {unCompedRow});
    final DenseMatrix64F c = new DenseMatrix64F(new double[][] {unCompedRow});
    mult(unCompedVector, compMatrix, c);
    compedRow = c.data;
    return compedRow;
  }

  public String[] getCompParameterNames() {
    return compParameters;
  }

  private double[][] parseSpillover(HashMap<String, String> keywords) {
    String spill = null;

    // Check for spillover keywords
    if (keywords.containsKey("$SPILLOVER")) {
      spill = keywords.get("SPILLOVER");
    } else if (keywords.containsKey("SPILL")) {
      spill = keywords.get("SPILL");
    } else {
      throw new NullPointerException("No spillover keyword found.");
    }

    // Magic string parsing from FCS Spec PDF
    final String[] s = spill.split(",");
    final int p = Integer.parseInt(s[0].trim());
    if (p >= 2) {
      final double[] spills = new double[p * p];
      int k = 0;
      for (int i = p + 1; i < spills.length + p + 1; i++) {
        spills[k] = Double.parseDouble(s[i]);
        k++;
      }
      final double[][] matrix = new double[p][p];
      final String[] compPars = new String[p];
      final Integer[] pMap = new Integer[p];
      for (int i = 0; i < compPars.length; i++) {
        compPars[i] = s[i + 1];
        final String parameterName = compPars[i];
        pMap[i] = FCSUtilities.findParameterNumnberByName(keywords, parameterName);
        final double[] row = new double[p];
        for (int j = 0; j < p; j++) {
          final int index = 1 + p + i * p + j;
          row[j] = Double.parseDouble(s[index]);
        }
        matrix[i] = row;
      }
      compParameterMap = pMap;
      compParameters = compPars;
      return matrix;
    } else {
      return null;
    }
  }

  public FCSFrame compensateFCSFrame(FCSFrame dataFrame) throws InvalidProtocolBufferException {
    FCSFrame newFrame = dataFrame.deepCopy();
    double[][] X = new double[compParameters.length][newFrame.getRowCount()];

    for (int i = 0; i < compParameters.length; i++) {
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(newFrame, compParameters[i]);
      if (dimension == null) {
        for (String s : compParameters) {
          System.out.println(s);
        }
        RuntimeException e = new RuntimeException(
            "DataFrame does not contain matching parameters: " + compParameters[i]);
        e.printStackTrace();
        throw e;
      }
      X[i] = dimension.getData();
    }
    DenseMatrix64F XT = new DenseMatrix64F(X);
    CommonOps.transpose(XT);
    // not sure about mutability.
    mult(XT.copy(), compMatrix, XT);
    CommonOps.transpose(XT);

    X = new double[compParameters.length][newFrame.getRowCount()];
    for (int i = 0; i < compParameters.length; i++) {
      for (int j = 0; j < newFrame.getRowCount(); j++) {
        double newVal = XT.get(i, j);
        X[i][j] = newVal;
      }
    }
    for (int i = 0; i < compParameters.length; i++) {
      FCSDimension dimension = FCSUtilities.findCompatibleDimension(newFrame, compParameters[i]);
      dimension.setData(X[i]);
      dimension.setShortName("[" + dimension.getShortName() + "]");
    }
    newFrame.setCompRef(this.getID());
    return newFrame;
  }

  public double[][] getMatrix() {
    return rawMatrix;
  }

  public String[] getInputDimensions() {
    // TODO Auto-generated method stub
    return compParameters;
  }

  public String[] getOutputDimensions() {
    String[] outDims = compParameters.clone();
    for (int i=0;i<outDims.length;i++){
      outDims[i] = "[" + outDims[i] + "]";
    }
    return outDims;
  }
  
  public double[] getSpilloverValues() {
    int inDimCount = getInputDimensions().length;
    int outDimCount= getOutputDimensions().length;
    double[] spills = new double[ inDimCount * outDimCount];
    for (int i=0;i<inDimCount;i++){
      for (int j=0;j<outDimCount;j++){
        spills[j*i+i] = rawMatrix[i][j];
      }
    }
    return spills;
  }
  
}
