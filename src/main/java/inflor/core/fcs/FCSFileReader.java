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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.utils.FCSUtilities;
import main.java.inflor.core.utils.MatrixUtilities;

public class FCSFileReader {

  private static final String DEFAULT_ENCODING = "UTF-8";
  // From Table 1 of FCS3.1 Spec. ANALYSIS and OTHER segments ignored.
  private static final int BEGIN_FCS_VERSION_OFFSET = 0;
  private static final int END_FCSVersionOffset = 5;

  private static final int FIRSTBYTE_BeginTextOffset = 10;
  private static final int LASTBYTE_BeginTextOffset = 17;
  private static final int FIRSTBYTE_EndTextOffset = 18;
  private static final int LASTBYTE_EndTextOffset = 25;

  private static final int FIRSTBYTE_BeginDataOffset = 26;
  private static final int LASTBYTE_BeginDataOffset = 33;
  private static final int FIRSTBYTE_EndDataOffset = 34;
  private static final int LASTBYTE_EndDataOffset = 41;
  private static final String ERROR_UNABLE_TO_CREATE_HASH = "Unable to create hashcode";

  public static boolean isValidFCS(String filePath) {
    boolean isValid = false;
    try {
      @SuppressWarnings("unused")
      final FCSFileReader reader = new FCSFileReader(filePath);
      isValid = true;
    } catch (final Exception e) {
      // noop
    }
    return isValid;
  }

  // file properties
  public final String pathToFile;
  public final RandomAccessFile FCSFile;
  public final Integer beginText;
  public final Integer endText;
  public final Integer beginData;
  public final String dataType;
  public final Integer[] bitMap;
  public final FCSFrame columnStore;
  public final String[] fileDimensionList;
  TreeSet<FCSDimension> data;
  public String[] compParameterList = null;

  // Constructor
  public FCSFileReader(String path_to_file) throws Exception {
    // Open the file
    pathToFile = path_to_file;
    final File f = new File(pathToFile);
    FCSFile = new RandomAccessFile(f, "r");

    // text specific properties
    beginText = readOffset(FIRSTBYTE_BeginTextOffset, LASTBYTE_BeginTextOffset);
    endText = readOffset(FIRSTBYTE_EndTextOffset, LASTBYTE_EndTextOffset);
    final HashMap<String, String> header = readHeader();
    header.put("FCSVersion", readFCSVersion(FCSFile));

    // Try to validate the header.
    if (FCSUtilities.validateHeader(header) == false) {
      final Exception e = new Exception("Invalid FCS Header.");
      e.printStackTrace();
      throw e;
    }

    fileDimensionList = FCSUtilities.parseDimensionList(header);

    final int rowCount = Integer.parseInt(header.get("$TOT"));
    columnStore = new FCSFrame(header, rowCount);

    // data specific properties
    beginData = readOffset(FIRSTBYTE_BeginDataOffset, LASTBYTE_BeginDataOffset);
    readOffset(FIRSTBYTE_EndDataOffset, LASTBYTE_EndDataOffset);
    bitMap = createBitMap(header);
    dataType = columnStore.getKeywordValue("$DATATYPE");
    data = new TreeSet<FCSDimension>();
  }

  private String calculateSHA(byte[] inBytes) throws NoSuchAlgorithmException {
    StringBuffer buffer = null;
    final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    messageDigest.update(inBytes);

    final byte[] bytes = messageDigest.digest();
    buffer = new StringBuffer();
    for (final byte b : bytes) {
      buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
    }

    return buffer.toString();
  }

  public void close() throws IOException {
    FCSFile.close();
  }

  private Integer[] createBitMap(HashMap<String, String> keywords) {
    // This method reads how many bytes per parameter and returns an integer
    // array of these values
    final String[] rawParameterNames = FCSUtilities.parseDimensionList(keywords);
    final Integer[] map = new Integer[rawParameterNames.length];
    for (int i = 1; i <= map.length; i++) {
      final String key = "$P" + (i) + "B";
      final String value = columnStore.getKeywordValue(key);
      final Integer byteSize = Integer.parseInt(value);
      map[i - 1] = byteSize;
    }
    return map;
  }

  public FCSFrame getColumnStore() {
    return columnStore;
  }

  public Map<String, String> getHeader() {
    return columnStore.getKeywords();
  }

  public void initRowReader() {
    try {
      FCSFile.seek(beginData);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public void readData() throws IOException {
    data = new TreeSet<>();
    FCSFile.seek(beginData);

    double[][] rawData = new double[columnStore.getRowCount()][fileDimensionList.length];
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

    columnStore.setData(data);
  }
  
  public void initColumnStoreNoData() throws IOException {
    data = new TreeSet<>();
    for (int i = 0; i < fileDimensionList.length; i++) {
      Integer pIndex = FCSUtilities.findParameterNumnberByName(getHeader(), fileDimensionList[i]);
      FCSDimension newDimension = FCSUtilities.buildFCSDimension(pIndex, getHeader());
      data.add(newDimension);
    }
    columnStore.setData(data);
  }

  public String readFCSVersion(RandomAccessFile raFile) throws IOException {
      FCSFile.seek(0);
      final byte[] bytes = new byte[END_FCSVersionOffset - BEGIN_FCS_VERSION_OFFSET + 1];
      raFile.read(bytes);
      return new String(bytes, DEFAULT_ENCODING);
  }

  private double[] readFloatRow(double[] row) throws IOException {
    for (int i = 0; i < row.length; i++) {
      final byte[] bytes = new byte[bitMap[i] / 8];
      FCSFile.read(bytes);
      row[i] = ByteBuffer.wrap(bytes).getFloat();
    }
    return row;
  }

  private HashMap<String, String> readHeader() throws IOException {
    // Delimiter is first UTF-8 character in the text section
    final byte[] delimiterBytes = new byte[1];
    FCSFile.seek(beginText);
    FCSFile.read(delimiterBytes);
    final String delimiter = new String(delimiterBytes);

    // Read the rest of the text bytes, this will contain the keywords
    final int textLength = endText - beginText + 1;
    final byte[] keywordBytes = new byte[textLength];

    FCSFile.read(keywordBytes);
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
    String sha256;
    try {
      sha256 = calculateSHA(keywordBytes);
      header.put("SHA-256", sha256);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new IOException(ERROR_UNABLE_TO_CREATE_HASH);
    }
    return header;
  }

  private double[] readIntegerRow(double[] row) throws IOException {
    for (int i = 0; i < row.length; i++) {
      Short I = null;
      final byte[] bytes = new byte[bitMap[i] / 8];
      FCSFile.read(bytes);
      I = ByteBuffer.wrap(bytes).getShort();
      row[i] = I;
    }
    return row;
  }

  private int readOffset(int start, int end) throws IOException {
    final byte[] bytes = new byte[end - start + 1];
    FCSFile.seek(start);
    FCSFile.read(bytes);
    final String s = new String(bytes, DEFAULT_ENCODING);
    final int offSet = Integer.parseInt(s.trim());
    return offSet;
  }

  public double[] readRow() throws IOException {
    /**
     * Reads the next row of the data.
     */

    double[] row = new double[fileDimensionList.length];
    if (dataType.equals("F")) {
      row = readFloatRow(row);
    } else if (dataType.equals("I")) {
      row = readIntegerRow(row);
    }
    return row;
  }

  public static FCSFrame read(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      reader.readData();
      return reader.getColumnStore();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static FCSFrame readNoData(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      reader.initColumnStoreNoData();
      return reader.getColumnStore();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static Map<String, String> readHeaderOnly(String filePath) {
    FCSFileReader reader;
    try {
      reader = new FCSFileReader(filePath);
      return reader.getHeader();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}