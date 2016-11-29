package io.landysh.inflor.main.core.dataStructures;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UninitializedMessageException;

import io.landysh.inflor.main.core.proto.FCSFrameProto.Message;
import io.landysh.inflor.main.core.proto.FCSFrameProto.Message.Dimension;
import io.landysh.inflor.main.core.proto.FCSFrameProto.Message.Keyword;
import io.landysh.inflor.main.core.proto.FCSFrameProto.Message.Transform;
import io.landysh.inflor.main.core.proto.FCSFrameProto.Message.Transform.Builder;
import io.landysh.inflor.main.core.transforms.AbstractTransform;
import io.landysh.inflor.main.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.main.core.transforms.LogicleTransform;
import io.landysh.inflor.main.core.transforms.LogrithmicTransform;
import io.landysh.inflor.main.core.transforms.TransformType;


// don't use the default serializer, there is a protobuf spec.
@SuppressWarnings("serial")
public class FCSFrame extends DomainObject implements Comparable<String> {

  private static final String DEFAULT_PREFFERED_NAME_KEYWORD = "$FIL";

  private TreeSet<FCSDimension> columnData;

  private HashMap<String, String> keywords;
  private String preferredName;
  private String compReference;

  // data properties
  private Integer rowCount = -1;

  private ArrayList<Subset> subsets;

  // minimal constructor, use with .load()
  public FCSFrame() {
    super(null);
  }

  /**
   * Store keywords and numeric columns in a persistable object.
   * 
   * @param inKeywords some annotation to get started with. Must be a valid FCS header but may be
   *        added to later.
   */
  public FCSFrame(HashMap<String, String> keywords, int rowCount) {
    this(null, keywords, rowCount);
  }

  public FCSFrame(String priorUUID, HashMap<String, String> keywords, int rowCount) {
    super(priorUUID);
    this.keywords = keywords;
    columnData = new TreeSet<FCSDimension>();
    this.rowCount = rowCount;
    preferredName = getKeywordValue(DEFAULT_PREFFERED_NAME_KEYWORD);
  }

  public void addDimension(FCSDimension newDim) {
    if (rowCount == newDim.getSize()) {
      columnData.add(newDim);
    } else {
      throw new IllegalStateException(
          "New dimension does not match frame size: " + rowCount.toString());
    }
  }

  public int getColumnCount() {
    return getColumnNames().size();
  }

  public List<String> getColumnNames() {
    List<String> columnNames = columnData
        .stream()
        .map(dimension -> dimension.getShortName())
        .collect(Collectors.toList());
    return columnNames;
  }

  public TreeSet<FCSDimension> getData() {
    return columnData;
  }
  
  public HashMap<String, String> getKeywords() {
    return keywords;
  }

  public String getKeywordValue(String keyword) {
    String result = null;
    try {
      result = keywords.get(keyword).trim();
    } catch (NullPointerException npe) {
      // No operatoin, just return a null value.
    }
    return result;
  }


  public String getPrefferedName() {
    String name = getID();
    if (this.preferredName != null) {
      name = this.preferredName;
    }
    return name;
  }

  public double[] getRow(int index) {
    final double[] row = new double[getColumnCount()];
    int i = 0;
    for (FCSDimension dim: columnData) {
      row[i] = dim.getData()[index];
      i++;
    }
    return row;
  }

  public int getRowCount() {
    return rowCount;
  }

  public FCSDimension getFCSDimensionByShortName(String shortName) {
    Optional<FCSDimension> matchingDimension = columnData
        .stream()
        .filter(dimension -> shortName.equals(dimension.getShortName()))
        .findAny();
    if (matchingDimension.isPresent()){
      return matchingDimension.get();
    } else {
      return null;
    }
  }

  public byte[] save() {
    // create the builder
    final Message.Builder messageBuilder = Message.newBuilder();
    messageBuilder.setId(this.getID());
    messageBuilder.setEventCount(this.rowCount);

    // add the dimension names.
    for (final String name : getColumnNames()) {
      messageBuilder.addDimNames(name);
    }

    // add the keywords.
    for (final String s : keywords.keySet()) {
      final String key = s;
      final String value = keywords.get(s);
      final Message.Keyword.Builder keyBuilder = Message.Keyword.newBuilder();
      keyBuilder.setKey(key);
      keyBuilder.setValue(value);
      final Message.Keyword newKeyword = keyBuilder.build();
      messageBuilder.addKeyword(newKeyword);
    }
    // add the FCS Dimensions.    
    for (FCSDimension dim : columnData){
      Message.Dimension.Builder dimBuilder = Message.Dimension.newBuilder();
      dimBuilder.setIndex(dim.getIndex());
      dimBuilder.setPnn(dim.getShortName());
      dimBuilder.setPns(dim.getStainName());
      dimBuilder.setPneF1(dim.getPNEF1());
      dimBuilder.setPneF2(dim.getPNEF2());
      dimBuilder.setPnr(dim.getRange());
      dimBuilder.setId(dim.getID());
      
      // Add the numeric data
      final double[] rawArray = dim.getData();
      for (final double value : rawArray) {
        dimBuilder.addData(value);
      }
      try {
        Builder tBuilder = buildTransform(dim);
        dimBuilder.setPreferredTransform(tBuilder.build());
      } catch (UninitializedMessageException e) {
        e.printStackTrace();
      }
      final Message.Dimension fcsdim = dimBuilder.build();
      messageBuilder.addDimension(fcsdim);
    }
    
    //Add subsets
    if (subsets!=null){
      for (Subset s:subsets){
        Message.Subset.Builder sBuilder = Message.Subset.newBuilder();
        sBuilder.setId(s.getID());
        sBuilder.setParentID(s.getParentID());
        sBuilder.setName(s.getLabel());
        BitSet mask = s.getMembers();
        for (int i=0;i<mask.size();i++){
          sBuilder.addMask(mask.get(i));
        }
      Message.Subset subset = sBuilder.build();
      messageBuilder.addSubset(subset);
      }
     
    }
    
    // build the message
    final Message buffer = messageBuilder.build();
    final byte[] bytes = buffer.toByteArray();
    return bytes;
  }

  private Builder buildTransform(FCSDimension fcsdim) {
    // Set the preferred transformation.
    Message.Transform.Builder tBuilder = Message.Transform.newBuilder();
    AbstractTransform transform = fcsdim.getPreferredTransform();
    if (transform instanceof LogicleTransform) {
      LogicleTransform logicle = (LogicleTransform) transform;
      tBuilder.setTransformType(TransformType.LOGICLE.toString());
      tBuilder.setLogicleT(logicle.getT());
      tBuilder.setLogicleW(logicle.getW());
      tBuilder.setLogicleM(logicle.getM());
      tBuilder.setLogicleA(logicle.getA());
    } else if (transform instanceof LogrithmicTransform) {
      LogrithmicTransform logTransform = (LogrithmicTransform) transform;
      tBuilder.setTransformType(TransformType.LOGARITHMIC.toString());
      tBuilder.setLogMin(logTransform.getMinRawValue());
      tBuilder.setLogMax(logTransform.getMaxRawValue());
    } else if (transform instanceof BoundDisplayTransform) {
      tBuilder.setTransformType(TransformType.BOUNDARY.toString());
      BoundDisplayTransform boundaryTransform = (BoundDisplayTransform) transform;
      tBuilder.setBoundMin(boundaryTransform.getMinRawValue());
      tBuilder.setLogMax(boundaryTransform.getMaxRawValue());
    }
    tBuilder.setId(transform.getID());
    return tBuilder;
  }

  public void save(FileOutputStream out) throws IOException {
    final byte[] message = this.save();
    out.write(message);
    out.flush();
  }

  public static FCSFrame load(byte[] bytes) throws InvalidProtocolBufferException {
    final Message loadedMessage = Message.parseFrom(bytes);

    // Load the keywords
    final HashMap<String, String> keywords = new HashMap<String, String>();
    for (int i = 0; i < loadedMessage.getKeywordCount(); i++) {
      final Keyword keyword = loadedMessage.getKeyword(i);
      final String key = keyword.getKey();
      final String value = keyword.getValue();
      keywords.put(key, value);
    }
    // Load the Dimensions
    final int rowCount = loadedMessage.getEventCount();
    final FCSFrame columnStore = new FCSFrame(loadedMessage.getId(), keywords, rowCount);
    final int dimCount = loadedMessage.getDimensionCount();
    final String[] dimen = new String[dimCount];
    for (int j = 0; j < dimCount; j++) {
      Dimension dim = loadedMessage.getDimension(j);
      String priorUUID = dim.getId();
      dimen[j] = priorUUID;
      final FCSDimension currentDimension =
          new FCSDimension(priorUUID, columnStore.getRowCount(), dim.getIndex(), dim.getPnn(),
              dim.getPns(), dim.getPneF1(), dim.getPneF2(), dim.getPnr());
      for (int i = 0; i < currentDimension.getSize(); i++) {
        currentDimension.getData()[i] = dim.getData(i);
      }
      AbstractTransform preferredTransform = readTransform(dim);
      currentDimension.setPreferredTransform(preferredTransform);
      columnStore.addDimension(currentDimension);
    }
    
    //TODO: Read back the subsets
    if (loadedMessage.getSubsetCount()>0){
      for (int i=0;i<loadedMessage.getSubsetCount();i++){
        Message.Subset subsetMessage = loadedMessage.getSubset(i);
        BitSet mask = new BitSet(subsetMessage.getMaskCount());
        for (int j=0;j<mask.size();j++){
          if (subsetMessage.getMask(j)){
            mask.set(j);
          }
        }
        Subset subset = new Subset(subsetMessage.getName(), 
                                   mask, 
                                   subsetMessage.getParentID(),
                                   subsetMessage.getId());
      columnStore.addSubset(subset);
      }
    }
    columnStore.setPreferredName(columnStore.getKeywordValue(DEFAULT_PREFFERED_NAME_KEYWORD));
    return columnStore;
  }

  private static AbstractTransform readTransform(Dimension dim) {
    if (dim.hasPreferredTransform()) {
      Transform tBuffer = dim.getPreferredTransform();
      if (tBuffer.getTransformType().equals(TransformType.LOGICLE.toString())) {
        double t = tBuffer.getLogicleT();
        double w = tBuffer.getLogicleW();
        double m = tBuffer.getLogicleM();
        double a = tBuffer.getLogicleA();
        LogicleTransform transform = new LogicleTransform(t, w, m, a);
        return transform;
      } else if (tBuffer.getTransformType().equals(TransformType.LOGARITHMIC.toString())) {
        LogrithmicTransform transform =
            new LogrithmicTransform(tBuffer.getLogMin(), tBuffer.getLogMax());
        return transform;

      } else if (tBuffer.getTransformType().equals(TransformType.BOUNDARY.toString())) {
        BoundDisplayTransform transform =
            new BoundDisplayTransform(tBuffer.getBoundMin(), tBuffer.getBoundMax());
        return transform;
      } else {
        System.out.print("Oh shit.");
      }
    }
    return null;
  }

  public static FCSFrame load(FileInputStream input) throws Exception {
    final byte[] buffer = new byte[input.available()];
    input.read(buffer);
    final FCSFrame columnStore = FCSFrame.load(buffer);
    return columnStore;
  }

  public void setData(TreeSet<FCSDimension> allData) {
    columnData = allData;
  }

  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  @Override
  public String toString() {
    return getPrefferedName();
  }

  public void setCompRef(String id) {
    this.compReference = id;
  }

  public String getCompRef() {
    return this.compReference;
  }

  @Override
  public int compareTo(String arg0) {
    return this.getPrefferedName().compareTo(arg0);
  }

  public FCSFrame deepCopy() throws InvalidProtocolBufferException {
    byte[] bytes = this.save();
    FCSFrame newFrame = FCSFrame.load(bytes);
    return newFrame;
  }

  public void addSubset(Subset subset) {
    if (this.subsets==null){
      this.subsets = new ArrayList<Subset>();
    }
    this.subsets.add(subset);
  }

  public List<Subset> getSubsets() {
    return subsets;
  }
}
