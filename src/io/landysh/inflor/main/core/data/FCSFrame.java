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
package io.landysh.inflor.main.core.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.protobuf.InvalidProtocolBufferException;

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

  private static final String LOAD_FAILURE = "Failed to de-serialize FCS Frame";

  private SortedSet<FCSDimension> columnData;

  private Map<String, String> keywords;
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
  public FCSFrame(Map<String, String> keywords, int rowCount) {
    this(null, keywords, rowCount);
  }

  public FCSFrame(String priorUUID, Map<String, String> keywords, int rowCount) {
    super(priorUUID);
    this.keywords = keywords;
    columnData = new TreeSet<>();
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
    return columnData
        .stream()
        .map(dimension -> dimension.getShortName())
        .collect(Collectors.toList());
  }

  public SortedSet<FCSDimension> getData() {
    return columnData;
  }
  
  public Map<String, String> getKeywords() {
    return keywords;
  }

  public String getKeywordValue(String keyword) {
    return keywords.get(keyword).trim();
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
    for (Entry<String, String> s : keywords.entrySet()) {
      final String key = s.getKey();
      final String value = s.getValue();
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
      Builder tBuilder = buildTransform(dim);
      dimBuilder.setPreferredTransform(tBuilder.build());

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
    return buffer.toByteArray();
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
      tBuilder.setBoundMin(boundaryTransform.getMinTranformedValue());
      tBuilder.setBoundMax(boundaryTransform.getMaxTransformedValue());
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
    final HashMap<String, String> keywords = new HashMap<>();
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
    
    //Read back the subsets
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
        return new LogicleTransform(t, w, m, a);
      } else if (tBuffer.getTransformType().equals(TransformType.LOGARITHMIC.toString())) {            
        return new LogrithmicTransform(tBuffer.getLogMin(), tBuffer.getLogMax());

      } else if (tBuffer.getTransformType().equals(TransformType.BOUNDARY.toString())) {
        return new BoundDisplayTransform(tBuffer.getBoundMin(), tBuffer.getBoundMax());
      } else {
        throw new IllegalArgumentException("Unsupported transform type.");
      }
    }
    return null;
  }

  public static FCSFrame load(FileInputStream input) throws IOException {
    final byte[] buffer = new byte[input.available()];
    int bytesRead = input.read(buffer);
    if (bytesRead==buffer.length){
      return FCSFrame.load(buffer);
    } else {
      throw new IOException(LOAD_FAILURE);
    }
  }

  public void setData(SortedSet<FCSDimension> sortedSet) {
    columnData = sortedSet;
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
    return FCSFrame.load(bytes);
  }

  public void addSubset(Subset subset) {
    if (this.subsets==null){
      this.subsets = new ArrayList<>();
    }
    this.subsets.add(subset);
  }

  public List<Subset> getSubsets() {
    return subsets;
  }
}
