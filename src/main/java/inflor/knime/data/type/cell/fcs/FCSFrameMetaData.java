package inflor.knime.data.type.cell.fcs;

import java.io.Serializable;
import java.util.List;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.data.Subset;
import inflor.core.transforms.TransformSet;

public class FCSFrameMetaData implements Serializable{
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final String   id;
  private final String   displayName;
  private final String[] dimensionKeys;
  private final String[] dimensionLabels;
  private final int messageSize;

  private final String   multiLineString;
  private Integer rowCount;
private TransformSet transformMap;

  public FCSFrameMetaData (FCSFrame dataFrame, int messageSize) {
      this.transformMap = new TransformSet();
	  String description = createMultilineDescription(dataFrame);
      multiLineString = description;
      id = dataFrame.getID();
      displayName = dataFrame.getDisplayName();
      dimensionKeys = dataFrame.getDimensionNames().toArray(new String[dataFrame.getDimensionCount()]);
      dimensionLabels = findDimensionDisplayNames(dataFrame, dimensionKeys);
      this.messageSize = messageSize;
      this.rowCount = dataFrame.getRowCount();
  }
  
  public FCSFrameMetaData (String id, String displayName, String[] dimensionKeys, String[] dimensionDisplayNames, String description, int messageSize, Integer rowCount2, TransformSet transforms) {
    this.multiLineString = description;
    this.id = id;
    this.displayName = displayName;
    this.dimensionKeys = dimensionKeys;
    this.dimensionLabels = dimensionDisplayNames;
    this.messageSize = messageSize;
    this.rowCount = rowCount2;
    this.transformMap = transforms;
}

  private String[] findDimensionDisplayNames(FCSFrame dataFrame, String[] dimKeys) {
    String[] displayNames = new String[dimKeys.length];
    for (int i=0;i<dimKeys.length;i++){
      String nName = dataFrame.getDimension(dimKeys[i]).getDisplayName();
      if (nName!=null){
        displayNames[i] = nName;
      } else {
        displayNames[i] = dimKeys[i];
      }
    }
    return displayNames;
  }

  private String createMultilineDescription(FCSFrame dataFrame) {
    StringBuilder sBuilder =  new StringBuilder();
    sBuilder.append(createFileSummary(dataFrame)); 
    sBuilder.append(createDimensionSummary(dataFrame));    
    sBuilder.append(createSubsetSummary(dataFrame));
    return sBuilder.toString();
  }


  private String createSubsetSummary(FCSFrame dataFrame) {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("\n");
    List<Subset> subsets = dataFrame.getSubsets();
    if (subsets!=null){
      for (Subset sub:  dataFrame.getSubsets()){
        sBuilder.append(sub.getLabel() + "-" + sub.getMembers().cardinality());
        sBuilder.append(" ");
      }
    }
    return sBuilder.toString().trim();
  }

  private String createDimensionSummary(FCSFrame dataFrame) {
    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append("\n");
    FCSDimension[] arr = dataFrame.getData().toArray(new FCSDimension[dataFrame.getDimensionCount()]);
    for (FCSDimension dim : arr){
      sBuilder.append(dim.getDisplayName());
      sBuilder.append("    ");
      sBuilder.append(transformMap.get(dim.getShortName()).toString());
      sBuilder.append("\n");
    }
    return sBuilder.toString();
  }

  private String createFileSummary(FCSFrame dataFrame) {
    String fileSummary = "";

    fileSummary+=dataFrame.getDisplayName();
    fileSummary+="\n";
    fileSummary+= Integer.toString(dataFrame.getRowCount());
    fileSummary+="\n";
    String cyt = dataFrame.getKeywordValue("$CYT");
    if (cyt!=null){
      fileSummary+= cyt;
      fileSummary+="\n";
    }
    
    String date = dataFrame.getKeywordValue("$DATE");
    if (cyt!=null){
      fileSummary+= date;
      fileSummary+="\n";
    }

    String bTime = dataFrame.getKeywordValue("$BTIM");
    String eTime = dataFrame.getKeywordValue("$ETIM");
    if (bTime!=null&&eTime!=null){
      fileSummary+= bTime;
      fileSummary+=" - ";
      fileSummary+= eTime;

    }
    return fileSummary;
  }

  public String getMultilineDescription() {
    return multiLineString;
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
  
  public TransformSet getTransformSet(){
	  return transformMap;
  }
}