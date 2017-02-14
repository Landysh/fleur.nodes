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
package main.java.inflor.core.fcs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.core.utils.MatrixUtilities;

public class FCSFileReader {

  private static final Logger LOGGER = Logger.getLogger( FCSFileReader.class.getName() );

  
  private static final String DEFAULT_ENCODING = "UTF-8";
  // From Table 1 of FCS3.1 Spec. ANALYSIS and OTHER segments ignored.
  private static final int BEGIN_FCS_VERSION_OFFSET = 0;
  private static final int END_FCS_VERSION_OFFSSET = 5;

  private static final int BEGIN_BEGIN_TEXT_OFFSET = 10;
  private static final int END_END_TEXT_OFFSET = 17;
  
  private static final int FIRST_BYTE_ENDTEXT_OFFSET = 18;
  private static final int LAST_BYTE_ENDTEXT_OFFSET = 25;

  private static final int FIRST_BYTE_BEGINDATA_OFFSET = 26;
  private static final int LAST_BYTE_BEGINDATA_OFFSET = 33;
  
  private static final int FIRST_BYTE_END_DATA_OFFSET = 34;
  private static final int LAST_BYTE_END_DATA_OFFSET = 41;

  // file properties
  final String pathToFile;
  final RandomAccessFile fcsFile;
  final Integer beginText;
  final Integer endText;
  final Integer beginData;
  final String dataType;
  final Integer[] bitMap;
  final FCSFrame fcsFrame;
  final String[] fileDimensionList;
  TreeSet<FCSDimension> data;
  String[] compParameterList = null;

  public FCSFileReader(String filePath) throws IOException {
    // Open the file
    pathToFile = filePath;
    final File f = new File(pathToFile);
    fcsFile = new RandomAccessFile(f, "r");

    // text specific properties
    beginText = readOffset(BEGIN_BEGIN_TEXT_OFFSET, END_END_TEXT_OFFSET);
    endText = readOffset(FIRST_BYTE_ENDTEXT_OFFSET, LAST_BYTE_ENDTEXT_OFFSET);
    final HashMap<String, String> header = readHeader();
    header.put("FCSVersion", readFCSVersion(fcsFile));
    header.put(FCSUtilities.KEY_FILENAME, f.getName());

    fileDimensionList = FCSUtilities.parseDimensionList(header);

    final int rowCount = Integer.parseInt(header.get("$TOT"));
    fcsFrame = new FCSFrame(header, rowCount);

    // data specific properties
    beginData = readOffset(FIRST_BYTE_BEGINDATA_OFFSET, LAST_BYTE_BEGINDATA_OFFSET);
    readOffset(FIRST_BYTE_END_DATA_OFFSET, LAST_BYTE_END_DATA_OFFSET);
    bitMap = createBitMap(header);
    dataType = fcsFrame.getKeywordValue("$DATATYPE");
    data = new TreeSet<>();
  }
  
  public void close() throws IOException {
    fcsFile.close();
  }

  private Integer[] createBitMap(HashMap<String, String> keywords) {
    // This method reads how many bytes per parameter and returns an integer
    // array of these values
    final String[] rawParameterNames = FCSUtilities.parseDimensionList(keywords);
    final Integer[] map = new Integer[rawParameterNames.length];
    for (int i = 1; i <= map.length; i++) {
      final String key = "$P" + (i) + "B";
      final String value = fcsFrame.getKeywordValue(key);
      final Integer byteSize = Integer.parseInt(value);
      map[i - 1] = byteSize;
    }
    return map;
  }

  public FCSFrame getFCSFrame() {
    return fcsFrame;
  }

  public Map<String, String> getHeader() {
    return fcsFrame.getKeywords();
  }

  public void initRowReader() throws IOException {
     fcsFile.seek(beginData);
  }

  public void readData() throws IOException {
    data = new TreeSet<>();
    fcsFile.seek(beginData);

    double[][] rawData = new double[fcsFrame.getRowCount()][fileDimensionList.length];
    for (int i = 0; i < rawData.length; i++) {
      double[] row = readRow();
      rawData[i] = row;
    }

    double[][] transposedRawData = MatrixUtilities.transpose(rawData);

    for (int i = 0; i < fileDimensionList.length; i++) {
      Integer pIndex = FCSUtilities.findParameterNumnberByName(getHeader(), fileDimensionList[i]);
      FCSDimension newDimension = FCSUtilities.buildFCSDimension(pIndex, getHeader());
      newDimension.setData(transposedRawData[i]);
      data.add(newDimension);
    }

    fcsFrame.setData(data);
  }
  
  public void initializeFrame() throws IOException {
    data = new TreeSet<>();
    for (int i = 0; i < fileDimensionList.length; i++) {
      Integer pIndex = FCSUtilities.findParameterNumnberByName(getHeader(), fileDimensionList[i]);
      FCSDimension newDimension = FCSUtilities.buildFCSDimension(pIndex, getHeader());
      data.add(newDimension);
    }
    fcsFrame.setData(data);
  }

  public String readFCSVersion(RandomAccessFile raFile) throws IOException {
      fcsFile.seek(0);
      final byte[] bytes = new byte[END_FCS_VERSION_OFFSSET - BEGIN_FCS_VERSION_OFFSET + 1];
      raFile.read(bytes);
      return new String(bytes, DEFAULT_ENCODING);
  }

  private double[] readFloatRow(double[] row) throws IOException {
    for (int i = 0; i < row.length; i++) {
      final byte[] bytes = new byte[bitMap[i] / 8];
      fcsFile.read(bytes);
      row[i] = ByteBuffer.wrap(bytes).getFloat();
    }
    return row;
  }

  private HashMap<String, String> readHeader() throws IOException {
    // Delimiter is first UTF-8 character in the text section
    final byte[] delimiterBytes = new byte[1];
    fcsFile.seek(beginText);
    fcsFile.read(delimiterBytes);
    final String delimiter = new String(delimiterBytes);

    // Read the rest of the text bytes, this will contain the keywords
    final int textLength = endText - beginText + 1;
    final byte[] keywordBytes = new byte[textLength];

    fcsFile.read(keywordBytes);
    String rawKeywords = new String(keywordBytes, DEFAULT_ENCODING);
    if (rawKeywords.length() > 0
        && rawKeywords.charAt(rawKeywords.length() - 1) == delimiter.charAt(0)) {
      rawKeywords = rawKeywords.substring(0, rawKeywords.length() - 1);
    }
    final StringTokenizer s = new StringTokenizer(rawKeywords, delimiter);
    final HashMap<String, String> header = new HashMap<>();
    Boolean ok = true;
    while (s.hasMoreTokens() && ok) {
      final String key = s.nextToken().trim();
      if (key.trim().isEmpty()) {
        ok = false;
      } else {
        final String value = s.nextToken().trim();
        header.put(key, value);
      }
    }
    
    HashFunction md = Hashing.sha256();
    HashCode code = md.hashBytes(keywordBytes);
    header.put("SHA-256", code.toString());
    return header;
  }

  private double[] readIntegerRow(double[] row) throws IOException {
    for (int i = 0; i < row.length; i++) {
      Short shortI;
      final byte[] bytes = new byte[bitMap[i] / 8];
      fcsFile.read(bytes);
      shortI = ByteBuffer.wrap(bytes).getShort();
      row[i] = shortI;
    }
    return row;
  }

  private int readOffset(int start, int end) throws IOException {
    final byte[] bytes = new byte[end - start + 1];
    fcsFile.seek(start);
    fcsFile.read(bytes);
    final String s = new String(bytes, DEFAULT_ENCODING);
    return Integer.parseInt(s.trim());
  }

  public double[] readRow() throws IOException {
    /**
     * Reads the next row of the data.
     */

    double[] row = new double[fileDimensionList.length];
    if ("F".equals(dataType)) {
      row = readFloatRow(row);
    } else if ("I".equals(dataType)) {
      row = readIntegerRow(row);
    }
    return row;
  }

  public static FCSFrame read(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      reader.readData();
      return reader.getFCSFrame();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Unable to read file.", e);
      return null;
    }
  }
  
  public static FCSFrame readNoData(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      reader.initializeFrame();
      return reader.getFCSFrame();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Unable to read file.", e);
      return null;
    }
  }
  
  public static Map<String, String> readHeaderOnly(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      return reader.getHeader();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Header unreadable.", e);
      return new HashMap<>();
    }
  }

  public String getPathToFile() {
    return pathToFile;
  }

  public Integer getBeginText() {
    return beginText;
  }

  public Integer getEndText() {
    return endText;
  }

  public Integer getBeginData() {
    return beginData;
  }
  public String getDataType() {
    return dataType;
  }

  public static boolean isValidFCS(String filePath) {
    boolean isValid = false;
    try {
      FCSFileReader reader = new FCSFileReader(filePath);
      isValid = FCSUtilities.validateHeader(reader.getHeader());
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Invalid File:" + filePath, e);
      isValid = false;
    }
    return isValid;
  }
}