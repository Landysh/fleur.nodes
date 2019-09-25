/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package fleur.core.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import fleur.core.logging.LogFactory;
import fleur.core.proto.FCSFrameProto.Message;
import fleur.core.proto.FCSFrameProto.Message.Dimension;
import fleur.core.proto.FCSFrameProto.Message.Keyword;
import fleur.core.utils.FCSUtilities;

// don't use the default serializer, there is a protobuf spec.
@SuppressWarnings("serial")
public class FCSFrame extends DomainObject implements Comparable<String> {

  private static final String LOAD_FAILURE = "Failed to de-serialize FCS Frame";

  private TreeSet<FCSDimension> columnData;
  private Map<String, String> keywords;
  private String compReference;
  private Integer rowCount = -1;
  private ArrayList<Subset> subsets = new ArrayList<>();

  /**
   * Store keywords and numeric columns in a persistable object.
   * 
   * @param inKeywords some annotation to get started with. Must be a valid FCS header but may be
   *        added to later.
   */
  public FCSFrame(Map<String, String> keywords, int rowCount) {
    this(null, keywords, rowCount);
  }

  public FCSFrame(String priorUUID, Map<String, String> header, int rows) {
    super(priorUUID);
    keywords = header;
    columnData = new TreeSet<>();
    rowCount = rows;
  }

  // minimal constructor, use with .load()
  public FCSFrame() {
    super(null);
  }

  private static BitSet extractMaskFromSubsetMessage(Message.Subset subsetMessage) {
    long[] longs = new long[subsetMessage.getMaskCount()];
    for (int j = 0; j < longs.length; j++) {
      longs[j] = subsetMessage.getMask(j);
    }
    return BitSet.valueOf(longs);
  }

  public static FCSFrame load(byte[] bytes) throws InvalidProtocolBufferException {
    final Message loadedMessage = Message.parseFrom(bytes);

    // Load the keywords
    final HashMap<String, String> keywords = loadKeywords(loadedMessage);

    // Load the Dimensions
    final int rowCount = loadedMessage.getEventCount();
    final FCSFrame fcsFrame = new FCSFrame(loadedMessage.getId(), keywords, rowCount);
    final int dimCount = loadedMessage.getDimensionCount();
    final String[] dimen = new String[dimCount];
    for (int j = 0; j < dimCount; j++) {
      Dimension dim = loadedMessage.getDimension(j);
      String priorUUID = dim.getId();
      dimen[j] = priorUUID;
      final FCSDimension currentDimension = new FCSDimension(priorUUID, fcsFrame.getRowCount(),
          dim.getIndex(), dim.getPnn(), dim.getPns(), dim.getPneF1(), dim.getPneF2(), dim.getPnr());
      for (int i = 0; i < currentDimension.size(); i++) {
        currentDimension.getData()[i] = dim.getData(i);
      }
      fcsFrame.addDimension(currentDimension);
    }

    // load the subsets
    if (loadedMessage.getSubsetCount() > 0) {
      for (int i = 0; i < loadedMessage.getSubsetCount(); i++) {
        Message.Subset subsetMessage = loadedMessage.getSubset(i);
        Subset subset = loadSubset(subsetMessage);
        fcsFrame.addSubset(subset);
      }
    }
    fcsFrame.setDisplayName(FCSUtilities.chooseDisplayName(fcsFrame));
    return fcsFrame;
  }

  private static HashMap<String, String> loadKeywords(final Message loadedMessage) {
    final HashMap<String, String> keywords = new HashMap<>();
    for (int i = 0; i < loadedMessage.getKeywordCount(); i++) {
      final Keyword keyword = loadedMessage.getKeyword(i);
      final String key = keyword.getKey();
      final String value = keyword.getValue();
      keywords.put(key, value);
    }
    return keywords;
  }

  private static Subset loadSubset(Message.Subset subsetMessage) {
    BitSet mask = extractMaskFromSubsetMessage(subsetMessage);

    int dimensionCount = subsetMessage.getDimensionsCount();
    String[] dimensions = null;
    if (dimensionCount > 0) {
      dimensions = new String[dimensionCount];
      for (int j = 0; j < dimensionCount; j++)
        dimensions[j] = subsetMessage.getDimensions(j);
    }

    int descriptorCount = subsetMessage.getDoubleValueCount();
    Double[] descriptors = null;
    if (descriptorCount > 0) {
      descriptors = new Double[descriptorCount];
      for (int j = 0; j < descriptorCount; j++)
        descriptors[j] = subsetMessage.getDoubleValue(j);
    }

    return new Subset(subsetMessage.getName(), mask, subsetMessage.getParentID(),
        subsetMessage.getId(), subsetMessage.getSubsetType(), dimensions, descriptors);
  }

  public static FCSFrame load(FileInputStream input) throws IOException {
    final byte[] buffer = new byte[input.available()];
    int bytesRead = input.read(buffer);
    if (bytesRead == buffer.length) {
      return FCSFrame.load(buffer);
    } else {
      throw new IOException(LOAD_FAILURE);
    }
  }

  public void addDimension(FCSDimension newDim) {
    if (rowCount == newDim.size()) {
      columnData.add(newDim);
    } else {
      throw new IllegalStateException(
          "New dimension does not match frame size: " + rowCount.toString());
    }
  }

  public void addSubset(Subset subset) {
    this.subsets.add(subset);
  }

  @Override
  public int compareTo(String arg0) {
    return this.getDisplayName().compareTo(arg0);
  }

  public FCSFrame deepCopy() {
    byte[] bytes = this.saveAsBytes();
    try {
      return FCSFrame.load(bytes);
    } catch (InvalidProtocolBufferException e) {
      RuntimeException rte = new RuntimeException("Could not copy fcs frame.");
      rte.initCause(e);
      throw rte;
    }
  }

  public String getCompRef() {
    return this.compReference;
  }

  public TreeSet<FCSDimension> getData() {
    return columnData;
  }

  public int getDimensionCount() {
    return getDimensionNames().size();
  }

  public ArrayList<String> getDimensionNames() {
    return columnData.stream().map(FCSDimension::getShortName)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public double[] getRow(int index) {
    final double[] row = new double[getDimensionCount()];
    int i = 0;
    for (String shortName : getDimensionNames()) {
      FCSDimension dim = getDimension(shortName);
      row[i] = dim.getData()[index];
      i++;
    }
    return row;
  }

  public String getDisplayName() {
    return FCSUtilities.chooseDisplayName(this);
  }

  public FCSDimension getDimension(String shortName) {
    Optional<FCSDimension> matchingDimension = columnData.stream()
        .filter(dimension -> shortName.equals(dimension.getShortName())).findAny();
    if (matchingDimension.isPresent()) {
      return matchingDimension.get();
    } else {
      throw new RuntimeException("Parameter name: " + shortName + " not found in " + this.getDimensionNames().toString());
    }
  }

  public Map<String, String> getKeywords() {
    return keywords;
  }

  public String getKeywordValue(String keyword) {
    if (keywords.containsKey(keyword)) {
      return keywords.get(keyword).trim();
    } else {
      return null;
    }
  }

  public int getRowCount() {
    return rowCount;
  }

  public BitSet[] getSubsetMatrix() {
    List<Subset> subsets = getSubsets(true);
    BitSet[] subsetMatrix = new BitSet[subsets.size()];
    for (int i=0;i<subsets.size();i++){
      subsetMatrix[i] = subsets.get(i).getMembers();
    }
    return subsetMatrix;
  }

  public List<Subset> getSubsets(boolean copy) {
    if (copy){
      return subsets.stream().map(s -> s.deepCopy()).collect(Collectors.toList());
    }
    return subsets;
  }

  public boolean hasDimension(String shortName) {
    Iterator<FCSDimension> iter = columnData.iterator();
    while (iter.hasNext()) {
      if (iter.next().getShortName().equals(shortName)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasSubset(String subsetNameScatter) {
    for (Subset s : subsets) {
      if (s.getLabel().equals(subsetNameScatter)) {
        return true;
      }
    }
    return false;
  }

  public byte[] saveAsBytes() {
    final Message buffer = createMessage();
    return buffer.toByteArray();
  }

  public String saveAsString() {
    final Message buffer = createMessage();
    try {
      return JsonFormat.printer().print(buffer);
    } catch (InvalidProtocolBufferException e) {
      LogFactory.createLogger(this.getClass().getName()).log(Level.FINE,
          "Unable to serialize message to json.");
      return null;
    }
  }

  private Message createMessage() {
    // create the builder
    final Message.Builder messageBuilder = Message.newBuilder();
    messageBuilder.setId(this.getID());
    messageBuilder.setEventCount(this.rowCount);

    // add the dimension names.
    for (final String name : getDimensionNames()) {
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
    for (FCSDimension dim : columnData) {
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
      final Message.Dimension fcsdim = dimBuilder.build();
      messageBuilder.addDimension(fcsdim);
    }

    // Add subsets
    if (subsets != null) {
      subsets.forEach(subset -> saveSubset(messageBuilder, subset));
    }
    return messageBuilder.build();
  }

  private void saveSubset(final Message.Builder messageBuilder, Subset currentSubset) {
    Message.Subset.Builder sBuilder = Message.Subset.newBuilder();
    sBuilder.setId(currentSubset.getID());
    sBuilder.setParentID(currentSubset.getParentID());
    sBuilder.setName(currentSubset.getLabel());
    sBuilder.setSubsetType(currentSubset.getType());
    String overrideID = currentSubset.getOverrideID();
    if (overrideID != null) {
      sBuilder.setOverrideID(currentSubset.getOverrideID());
    }

    String[] dimensions = currentSubset.getDimensions();
    if (dimensions != null)
      sBuilder.addAllDimensions(Arrays.asList(dimensions));

    Double[] descriptors = currentSubset.getDescriptors();
    if (descriptors != null)
      sBuilder.addAllDoubleValue(Arrays.asList(descriptors));

    long[] longs = currentSubset.getMembers().toLongArray();
    for (int i = 0; i < longs.length; i++){
    	sBuilder.addMask(longs[i]);
    }
    Message.Subset subset = sBuilder.build();
    messageBuilder.addSubset(subset);
  }

  public int save(FileOutputStream out) throws IOException {
    final byte[] message = this.saveAsBytes();
    out.write(message);
    out.flush();
    return message.length;
  }

  public void setCompRef(String id) {
    this.compReference = id;
  }

  public void setData(TreeSet<FCSDimension> sortedSet) {
    columnData = sortedSet;
  }

  public void setDisplayName(String newDisplayName) {
    keywords.put(FCSUtilities.KEY_DISPLAY_NAME, newDisplayName);
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  public BitSet getFilteredFrame(String referenceSubset, boolean includeAnscestry) {
    Optional<Subset> targetSubset =
        subsets.stream().filter(sub -> sub.getLabel().equals(referenceSubset)).findAny();
    if (targetSubset.isPresent() && includeAnscestry) {
      Subset currentSubset = targetSubset.get();
      List<Subset> ancestors = currentSubset.findAncestors(getSubsets());
      BitSet mask = currentSubset.evaluate(ancestors);
      return mask;
    } else if (targetSubset.isPresent() && !includeAnscestry) {
      return targetSubset.get().getMembers();
    } else {
      return null;
    }
  }

  public List<Subset> getSubsets() {
    return getSubsets(true);
  }

  public List<String> getSubsetNames() {
    return subsets.stream().map(Subset::getLabel).collect(Collectors.toList());
  }

  public double[][] getMatrix(List<String> dimensionNames) {
    double[][] mtx = new double[dimensionNames.size()][rowCount];
    int i = 0;
    for (String name : dimensionNames) {
      FCSDimension dim = getDimension(name);
      mtx[i] = dim.getData();
      i++;
    }
    return mtx;
  }

  public static FCSFrame loadFromProtoString(String previewString)
      throws InvalidProtocolBufferException {
    fleur.core.proto.FCSFrameProto.Message.Builder mb = Message.newBuilder();
    JsonFormat.parser().merge(previewString, mb);
    return FCSFrame.load(mb.build().toByteArray());
  }

  public void setSubsets(ArrayList<Subset> mergedSubsets) {
    this.subsets = mergedSubsets;
  }
}
