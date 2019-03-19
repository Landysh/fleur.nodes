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
package inflor.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.data.Subset;
import inflor.core.fcs.FCSFileReader;
import inflor.core.fcs.DimensionTypes;
import inflor.core.singlets.PuleProperties;
import inflor.core.transforms.AbstractTransform;
import inflor.core.transforms.TransformSet;
import inflor.knime.core.NodeUtilities;

public class FCSUtilities {

  private static final String REGEX_IS_COMPENSATED = "\\[.*\\]";
  private static final String DEFAULT_PREFFERED_NAME_KEYWORD = "$FIL";
  public static final String KEY_FILENAME = "File name when read.";
  public static final String KEY_MERGE_MAP = "INFLOR_MERGE_MAP";
  public static final int MERGE_DIMENSION_INDEX = -2;
  public static final String MERGE_DIMENSION_NAME = "Merged_Samples";
  public static final CharSequence DELIMITER = "||";
  public static final String DELIMITER_REGEX = "\\|\\|";
  public static final String PROP_KEY_PREVIEW_FRAME = "Inflor Preview Frame";
  public static final String FCSKEY_EVENT_COUNT = "$TOT";
  public static final Integer DEFAULT_MAX_SUMMARY_FRAME_VALUES = 250000;
  public static final String KEY_DISPLAY_NAME = "Inflor Display Name";


  private FCSUtilities() {

  }

  public static Integer findParameterNumnberByName(Map<String, String> keywords, String name) {
    /**
     * Attempts to find the parameter number in a supplied FCS header (keywords). If the parameter
     * name is not it will return null.
     */
    Integer parameterIndex = -1;
    final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));
    for (int i = 1; i <= parameterCount; i++) {
      final String parameterKey = "$P" + i + "N";
      final String value = keywords.get(parameterKey);
      if (value.equals(name)) {
        parameterIndex = i;
        break;
      }
    }
    if (parameterIndex != -1) {
      return parameterIndex;
    } else {
      return null;
    }
  }

  public static String findStainName(Map<String, String> keywords, Integer parameterIndex) {
    /**
     * Returns the $PnS value for a particular parameter index. Will return an empty string if no
     * keyword is found.
     */
    String pnsKeyword = "$P" + (1 + parameterIndex) + "S";
    if (keywords.containsKey(pnsKeyword)) {
      return keywords.get(pnsKeyword);
    } else {
      return "";
    }
  }

  public static double[] filterColumn(BitSet mask, double[] data) {
    double[] filteredData = new double[mask.cardinality()];
    int currentBit = 0;
    for (int i = 0; i < filteredData.length; i++) {
      int nextBit = mask.nextSetBit(currentBit);
      filteredData[i] = data[nextBit];
      currentBit = nextBit + 1;
    }
    return filteredData;
  }

  public static String[] parseDimensionList(Map<String, String> keywords) {
    /**
     * Returns a String[] containing all of the values of the $PnN keywords from the specified
     * header table.
     */
    final String parCount = keywords.get("$PAR");
	final int columnCount = Integer.parseInt(parCount);
    final String[] plist = new String[columnCount];
    for (int i = 1; i <= columnCount; i++) {
      final String keyword = "$P" + i + "N";
      plist[i - 1] = keywords.get(keyword);
    }
    return plist;
  }

  public static Boolean validateHeader(Map<String, String> keywords) {
    /**
     * Returns a boolean indicating whether the specified keywords are consistent with the
     * requirements of the FCS Standard version 3.1.
     */

    // TODO: Check all required keywords.
    final boolean isFCS = keywords.get("FCSVersion").contains("FCS");
    final Integer rowCount = Integer.parseInt(keywords.get("$TOT"));
    final Integer parameterCount = Integer.parseInt(keywords.get("$PAR"));

    boolean isValid;
    if (isFCS && rowCount > 0 && parameterCount > 0) {
      isValid = true;
    } else {
      isValid = false;
    }
    return isValid;
  }

  public static Map<String, String> findParameterKeywords(Map<String, String> sourceKeywords,
      int parameterIndex) {
    HashMap<String, String> keywords = new HashMap<>();
    String regex = "\\$P" + parameterIndex + "[A-Z]";
    for (String key : keywords.keySet()) {
      if (key.matches(regex)) {
        keywords.put(key, sourceKeywords.get(key));
      }
    }
    return keywords;
  }

  public static FCSDimension buildFCSDimension(int pIndex, Map<String, String> header) {
    /**
     * Constructs a new FCSDimension object from the parameter data in the header of an FCS File.
     */
    int size = Integer.parseInt(header.get("$TOT"));
    String pnn = header.get("$P" + pIndex + "N");
    String pns = header.get("$P" + pIndex + "S");
    String pne = header.get("$P" + pIndex + "E");
    double pneF1 = Double.parseDouble(pne.split(",")[0]);
    double pneF2 = Double.parseDouble(pne.split(",")[1]);
    double pnr = Double.parseDouble(header.get("$P" + pIndex + "R"));

    return new FCSDimension(size, pIndex, pnn, pns, pneF1, pneF2, pnr);
  }

  public static FCSFrame filterFrame(BitSet mask, FCSFrame inFrame) {

    FCSFrame out = new FCSFrame(inFrame.getKeywords(), mask.cardinality());
    
    // Add subsets
    inFrame
      .getSubsets()
      .stream()
      .map(inSub -> inSub.filter(mask))
      .forEach(out::addSubset);
    // Add dimenions
    for (FCSDimension inDim : inFrame.getData()) {
      FCSDimension outDim =
          new FCSDimension(mask.cardinality(), inDim.getIndex(), inDim.getShortName(),
              inDim.getStainName(), inDim.getPNEF1(), inDim.getPNEF2(), inDim.getRange());
      outDim.setData(BitSetUtils.filter(inDim.getData(), mask));
      out.addDimension(outDim);
    }
    return out;
  }

  public static Optional<FCSDimension> findCompatibleDimension(FCSFrame dataSource,
      String shortName) {
    /**
     * Returns the key for the first compatible FCSDimension in the selected map. (ie. where the
     * result of the toString() method is the same). Will return null if no compatible entry is
     * found.
     */
    FCSDimension returnDim = null;
    for (FCSDimension dim : dataSource.getData()) {
      if (dim.getShortName().equals(shortName)) {
        returnDim = dim;
      }
    }
    return Optional.ofNullable(returnDim);
  }

  public static FCSFrame createSummaryFrame(List<FCSFrame> fcsList, Integer maxEventsPerFrame) {
    Optional<Integer> optDataSize =
        fcsList.stream().map(FCSFrame::getRowCount).min(Integer::compare);

    int minDataSize = optDataSize.isPresent() ? optDataSize.get() : 0;

    final Integer finalSize = (minDataSize > maxEventsPerFrame) ? maxEventsPerFrame : minDataSize;

    Optional<FCSFrame> optReturn =
        fcsList.stream().map(dataFrame -> FCSUtilities.downSample(dataFrame, finalSize))
            .reduce(new FCSConcatenator());

    if (optReturn.isPresent()) {
      FCSFrame df = optReturn.get();
      df.setDisplayName(NodeUtilities.PREVIEW_FRAME_KEY);
    } else {
      return null;
    }

    return optReturn.isPresent() ? optReturn.get() : null;
  }

  private static FCSFrame downSample(FCSFrame dataFrame, Integer dataSize) {
    if (dataFrame.getRowCount() <= dataSize) {
      return dataFrame.deepCopy();
    } else {
      BitSet mask = BitSetUtils.getShuffledMask(dataFrame.getRowCount(), dataSize);
      return FCSUtilities.filterFrame(mask, dataFrame);
    }
  }

  public static FCSDimension findPreferredDimensionType(FCSFrame fcsFrame,
      DimensionTypes dimensionType) {
    ArrayList<FCSDimension> forwardScatterDims = new ArrayList<>();
    for (FCSDimension dimension : fcsFrame.getData()) {
      boolean isForwardScatter = dimensionType.matches(dimension.getShortName());
      if (isForwardScatter) {
        forwardScatterDims.add(dimension);
      }
    }

    FCSDimension fscADimension = findDimensionType(forwardScatterDims, PuleProperties.AREA);
    FCSDimension fscHDimension = findDimensionType(forwardScatterDims, PuleProperties.HEIGHT);
    FCSDimension fscWDimension = findDimensionType(forwardScatterDims, PuleProperties.WIDTH);

    if (fscADimension != null) {
      return fscADimension;
    } else if (fscHDimension != null) {
      return fscHDimension;
    } else if (fscWDimension != null) {
      return fscWDimension;
    } else {
      return null;
    }
  }

  /**
   * 
   * @param a list of FCSDimensions
   * @param pulseTypes a PulseProperty such as HEIGHT, WIDTH, or AREA
   * @return returns an FCSDimension matching the pulseType (by regex) or @null
   */
  private static FCSDimension findDimensionType(ArrayList<FCSDimension> forwardScatterDims,
      PuleProperties pulseTypes) {
    FCSDimension foundDimension = null;
    if (!forwardScatterDims.isEmpty()) {
      Optional<FCSDimension> optionalDimension = forwardScatterDims.stream()
          .filter(dim -> pulseTypes.matches(dim.getShortName())).findAny();

      if (optionalDimension.isPresent()) {
        foundDimension = optionalDimension.get();
      }
    }
    return foundDimension;
  }

  public static String formatCompStainName(String origninalName) {
    return "[" + origninalName + "]";
  }

  public static Set<String> getDimensionNames(List<FCSFrame> dataSet) {
    HashSet<String> setDimensionNames = new HashSet<>();
    for (FCSFrame dataFrame : dataSet) {
      Set<String> frameDimensionNames =
          dataFrame.getData().stream().map(FCSDimension::getShortName).collect(Collectors.toSet());
      setDimensionNames.addAll(frameDimensionNames);
    }
    return setDimensionNames;
  }

  public static boolean isCompensated(String compParName) {
    return compParName.matches(REGEX_IS_COMPENSATED);
  }

  public static boolean isFluorescent(String shortName) {
    boolean isFluorescent = true;
    if (DimensionTypes.TIME.matches(shortName) || DimensionTypes.FORWARD_SCATTER.matches(shortName)
        || DimensionTypes.SIDE_SCATTER.matches(shortName)) {
      isFluorescent = false;
    }
    return isFluorescent;
  }

  public static List<FCSFrame> readValidFiles(String path) {
    /**
     * Returns an unsorted list of valid FCS Files from the chosen directory.
     */
    final File folder = new File(path);
    final File[] files = folder.listFiles();

    return Arrays.asList(files).stream().parallel().map(File::getAbsolutePath)
        .filter(FCSFileReader::isValidFCS).map(FCSFileReader::read).collect(Collectors.toList());
  }

  public static String chooseDisplayName(FCSFrame fcsFrame) {
    Map<String, String> keywords = fcsFrame.getKeywords();
    String name;
    if (keywords.containsKey(FCSUtilities.KEY_DISPLAY_NAME)) {
      name = keywords.get(FCSUtilities.KEY_DISPLAY_NAME);
    } else if (keywords.containsKey(FCSUtilities.DEFAULT_PREFFERED_NAME_KEYWORD)) {
      name = keywords.get(FCSUtilities.DEFAULT_PREFFERED_NAME_KEYWORD);
    } else {
      name = fcsFrame.getID();
    }
    return name;
  }

  public static Subset findCompatibleSubset(FCSFrame df, String mReferenceSubset) {
    Optional<Subset> opt =
        df.getSubsets().stream().filter(s -> s.getLabel().equals(mReferenceSubset)).findAny();
    if (opt.isPresent()) {
      return opt.get();
    } else {
      return null;
    }
  }

  public static void transformMatrix(List<String> dimensionNames, TransformSet transforms,
      double[][] mtx) {
    for (int i = 0; i < dimensionNames.size(); i++) {
      AbstractTransform at = transforms.get(dimensionNames.get(i));
      mtx[i] = at.transform(mtx[i]);
    }

  }
}
