/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package inflor.core.compensation;

import static org.ejml.ops.CommonOps.invert;
import static org.ejml.ops.CommonOps.mult;

import java.util.Map;
import java.util.Optional;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import com.google.protobuf.InvalidProtocolBufferException;

import inflor.core.data.DomainObject;
import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.utils.FCSUtilities;

@SuppressWarnings("serial")
public class SpilloverCompensator extends DomainObject {

  private static final double COMP_MATRIX_ZERO_THRESHOLD = 0.000001;
  // Compensation details
  private String[] compParameters;
  private DenseMatrix64F compMatrix;
  private double[][] rawMatrix;

  public SpilloverCompensator(Map<String, String> referenceHeader) {
    this(referenceHeader, null);
  }

  public SpilloverCompensator(Map<String, String> keywords, String priorUUID) {
    super(priorUUID);
    rawMatrix = parseSpillover(keywords);
    compMatrix = new DenseMatrix64F(rawMatrix);
    invert(compMatrix);
    validate();
  }

  public SpilloverCompensator(String[] inDimensionList, String[] outDimensionList,
      Double[] spilloverValues, String priorUUID) {
    super(priorUUID);
    compParameters = inDimensionList;
    rawMatrix = new double[inDimensionList.length][outDimensionList.length];
    for (int i=0;i<inDimensionList.length;i++){
      for (int j=0;j<outDimensionList.length;j++){
        int flatIndex = i*outDimensionList.length + j;
        double value = spilloverValues[flatIndex];
        rawMatrix[i][j] = value;
      }
    }
    compMatrix = new DenseMatrix64F(rawMatrix);
    invert(compMatrix);
  }
  
  public SpilloverCompensator(String[] dimensionList, String[] outDimensionList,
      Double[] spilloverValues) {
    this(dimensionList, outDimensionList, spilloverValues, null);
  }

  private void validate() {
    if (compParameters.length <= 1) {
      throw new IllegalArgumentException("Invalid compensation matrix");
    }
  }

  public String[] getCompParameterNames() {
    return compParameters;
  }

  private double[][] parseSpillover(Map<String, String> keywords) {

    String spill;
    // TODO: double check this, it looks wrong. Extract constants. 
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
      for (int i = 0; i < compPars.length; i++) {
        compPars[i] = s[i + 1];
        final double[] row = new double[p];
        for (int j = 0; j < p; j++) {
          final int index = 1 + p + i * p + j;
          row[j] = Double.parseDouble(s[index]);
        }
        matrix[i] = row;
      }
      compParameters = compPars;
      return matrix;
    } else {
      throw new IllegalArgumentException("Invalid Spillover Keyword: Matrix must contain at least 2 input dimensions");
    }
  }

  public FCSFrame compensateFCSFrame(FCSFrame dataFrame, boolean retainUncomped) throws InvalidProtocolBufferException {
    FCSFrame newFrame = dataFrame.deepCopy();
    double[][] x = new double[compParameters.length][newFrame.getRowCount()];

    for (int i = 0; i < compParameters.length; i++) {
      Optional<FCSDimension> dimension = FCSUtilities.findCompatibleDimension(newFrame, compParameters[i]);
      if (!dimension.isPresent()) {
        throw new IllegalArgumentException("DataFrame does not contain matching parameters: " + compParameters[i]);
      }
      x[i] = dimension.get().getData();
    }
    DenseMatrix64F xt = new DenseMatrix64F(x);
    CommonOps.transpose(xt);
    // not sure about mutability.
    mult(xt.copy(), compMatrix, xt);
    CommonOps.transpose(xt);

    x = new double[compParameters.length][newFrame.getRowCount()];
    for (int i = 0; i < compParameters.length; i++) {
      for (int j = 0; j < newFrame.getRowCount(); j++) {
        double newVal = xt.get(i, j);
        x[i][j] = newVal;
      }
    }
    for (int i = 0; i < compParameters.length; i++) {
      Optional<FCSDimension> dimension = FCSUtilities.findCompatibleDimension(newFrame, compParameters[i]);
      dimension.get().setData(x[i]);
      dimension.get().setShortName("[" + dimension.get().getShortName() + "]");
    }
    newFrame.setCompRef(this.getID());
    
    if (retainUncomped){
      for (int i = 0; i < compParameters.length; i++) {
        Optional<FCSDimension> dimension = FCSUtilities.findCompatibleDimension(dataFrame, compParameters[i]);
        newFrame.addDimension(dimension.get());
      }
    }
    
    
    return newFrame;
  }

  public double[][] getMatrix() {
    return rawMatrix;
  }

  public String[] getInputDimensions() {
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
        spills[i*outDimCount+j] = rawMatrix[i][j];
      }
    }
    return spills;
  }

  public boolean isEmpty() {
    boolean isEmpty = true;
    for (double d:getSpilloverValues()){
      if (d >= COMP_MATRIX_ZERO_THRESHOLD ){
        isEmpty = false; 
        break;
      }
    }
    return isEmpty;
  }
}
