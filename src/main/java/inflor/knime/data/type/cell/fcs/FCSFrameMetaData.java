package inflor.knime.data.type.cell.fcs;

import java.io.Serializable;

import inflor.core.data.FCSFrame;
import inflor.core.transforms.TransformSet;

public class FCSFrameMetaData implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final String id;
  private final String displayName;
  private Integer rowCount;
  private final String[] dimensionKeys;
  private final String[] dimensionLabels;
  private String[] subsetNames;
  private TransformSet transformMap;
  private final int messageSize;
  
  private FCSFrameMetaData(FCSFrameMetaData meta){
	  this.id = meta.id;
	  this.displayName = meta.displayName;
	  this.rowCount = meta.rowCount;
	  this.dimensionKeys = meta.dimensionKeys.clone();
	  this.dimensionLabels = meta.dimensionLabels.clone();
	  this.subsetNames = meta.subsetNames.clone();
	  this.transformMap = meta.transformMap.deepCopy();
	  this.messageSize = meta.messageSize;
  }


  public FCSFrameMetaData(FCSFrame dataFrame, int messageSize) {
    this.transformMap = new TransformSet();

    id = dataFrame.getID();
    displayName = dataFrame.getDisplayName();
    dimensionKeys =
        dataFrame.getDimensionNames().toArray(new String[dataFrame.getDimensionCount()]);
    dimensionLabels = findDimensionDisplayNames(dataFrame, dimensionKeys);
    this.subsetNames = dataFrame.getSubsetNames().toArray(new String[dataFrame.getSubsetNames().size()]);
    this.messageSize = messageSize;
    this.rowCount = dataFrame.getRowCount();
  }

  public FCSFrameMetaData(String id, String displayName, String[] dimensionKeys,
      String[] dimensionDisplayNames, String[] subsetNames, int messageSize, Integer rowCount2,
      TransformSet transforms) {
    this.id = id;
    this.displayName = displayName;
    this.dimensionKeys = dimensionKeys;
    this.dimensionLabels = dimensionDisplayNames;
    this.messageSize = messageSize;
    this.rowCount = rowCount2;
    this.transformMap = transforms;
    this.subsetNames = subsetNames;
  }

  private String[] findDimensionDisplayNames(FCSFrame dataFrame, String[] dimKeys) {
    String[] displayNames = new String[dimKeys.length];
    for (int i = 0; i < dimKeys.length; i++) {
      String nName = dataFrame.getDimension(dimKeys[i]).getDisplayName();
      if (nName != null) {
        displayNames[i] = nName;
      } else {
        displayNames[i] = dimKeys[i];
      }
    }
    return displayNames;
  }

  private String createSubsetSummary() {
    return String.join("\n", subsetNames);
  }

  private String createDimensionSummary() {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("\n");
    for (int i = 0; i < dimensionKeys.length; i++) {
      sBuilder.append(dimensionLabels[i]);
      sBuilder.append("    ");
      sBuilder.append(transformMap.get(dimensionKeys[i]));
      sBuilder.append("\n");
    }
    return sBuilder.toString();
  }

  private String createFileSummary() {
    String fileSummary = "";

    fileSummary += displayName;
    fileSummary += "\n";
    fileSummary += Integer.toString(rowCount);
    fileSummary += "\n";
    return fileSummary;
  }

  public String getMultilineDescription() {
    return createFileSummary() + createDimensionSummary() + createSubsetSummary();
  }

  public String getID() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String[] getDimensionNames() {
    return dimensionKeys;
  }

  public String[] getDimensionLabels() {
    return dimensionLabels;
  }

  public int getSize() {
    return messageSize;
  }

  public Integer getRowCount() {
    return rowCount;
  }

  public void setTransforms(TransformSet transforms) {
    transformMap = transforms;
  }

  public TransformSet getTransformSet() {
    return transformMap;
  }

  public String[] getSubsetNames() {
    return subsetNames;
  }
  
  public FCSFrameMetaData copy(){
	  return new FCSFrameMetaData(this);
  }
}
