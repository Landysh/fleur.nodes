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
package fleur.core.fcs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import fleur.core.data.FCSDimension;
import fleur.core.data.FCSFrame;
import inflor.core.logging.LogFactory;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.MatrixUtilities;

public class FCSFileReader {

  private static final Logger LOGGER = Logger.getLogger(FCSFileReader.class.getName());


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

  // FCS Standard Keywords and Values.
  public static final String BYTE_ORDER_KEY = "$BYTEORD";
  // The values of the BYTEORD keyword have been restricted to either 1,2,3,4 (little endian) or
  public static final String BYTE_ORDER_LITTLE = "1,2,3,4";
  // 4,3,2,1 (big endian).
  public static final String BYTE_ORDER_BIG = "4,3,2,1";
  // Ahem. mother flower.
  public static final String BYTE_ORDER_MIX = "3,4,1,2";


  public static final String EVENT_COUNT_KEY = "$TOT";
  public static final String DATATYPE_KEY = "$DATATYPE";
  public static final String DATATYPE_FLOAT = "F";
  public static final String DATATYPE_INT = "I";

  // Fleur specific keys
  private static final String DOUBLE_DELIM_TOKEN = "FLEURDELIM";
  public static final String FCS_VERSION_KEY = "FCSVersion";


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

  private String endianness = BYTE_ORDER_LITTLE;

  public FCSFileReader(String filePath, RandomAccessFile raf) throws Exception {
    this(filePath, raf, false, false);
  }

  
  public FCSFileReader(String filePath, RandomAccessFile raf, boolean compOnRead, boolean dropUncomped) throws Exception {
    // Open the file
    pathToFile = filePath;
    final File f = new File(pathToFile);
    fcsFile = raf;

    // do some sanity checking on the file before trying to parse it
    if (fcsFile.length() <= 100) {
      throw new Exception("File length < 100 bytes, this is very likely not valid.");
    }

    // text specific properties
    beginText = readOffset(BEGIN_BEGIN_TEXT_OFFSET, END_END_TEXT_OFFSET);
    endText = readOffset(FIRST_BYTE_ENDTEXT_OFFSET, LAST_BYTE_ENDTEXT_OFFSET);
    final HashMap<String, String> header = readHeader();
    header.put(FCS_VERSION_KEY, readFCSVersion(fcsFile));
    header.put(FCSUtilities.KEY_FILENAME, f.getName());

    // set endian flag based on required keyword.
    endianness = header.get(BYTE_ORDER_KEY);

    fileDimensionList = FCSUtilities.parseDimensionList(header);

    final int rowCount = Integer.parseInt(header.get(EVENT_COUNT_KEY));
    fcsFrame = new FCSFrame(header, rowCount);

    // data specific properties
    beginData = readOffset(FIRST_BYTE_BEGINDATA_OFFSET, LAST_BYTE_BEGINDATA_OFFSET);
    readOffset(FIRST_BYTE_END_DATA_OFFSET, LAST_BYTE_END_DATA_OFFSET);
    bitMap = createBitMap(header);
    dataType = fcsFrame.getKeywordValue(DATATYPE_KEY);
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
    rawKeywords = scrubKeywords(rawKeywords, delimiter);

    final StringTokenizer s = new StringTokenizer(rawKeywords, delimiter);
    final HashMap<String, String> header = new HashMap<>();
    Boolean ok = true;
    while (s.hasMoreTokens() && ok) {
      final String key = s.nextToken().trim();
      if (key.trim().isEmpty()) {
        ok = false;
      } else {
        try {
          final String value = s.nextToken().trim();
          header.put(unScrubKeywords(key, delimiter), unScrubKeywords(value, delimiter));
        } catch (NoSuchElementException e) {
          String message = "Keyword value for: " + key
              + " does not exist.  Header appears to be malformed, proceed with some caution.";
          LogFactory.createLogger(this.getClass().getName()).log(Level.FINE, message, e);
        }
      }
    }



    HashFunction md = Hashing.sha256();
    HashCode code = md.hashBytes(keywordBytes);
    header.put("SHA-256", code.toString());
    return header;
  }

  private String scrubKeywords(String rawKeywords, String delimiter) {
    String cleantext = rawKeywords;
    // Handle the case of two consecutive delimiters.
    String doubleD = delimiter + delimiter;
    if (cleantext.contains(doubleD)) {
      cleantext = cleantext.replace(doubleD, DOUBLE_DELIM_TOKEN);
    }
    return cleantext;
  }

  private String unScrubKeywords(String input, String delimiter) {
    String output = input.replace(DOUBLE_DELIM_TOKEN, input);
    return output;
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
    for (int i = 0; i < row.length; i++) {
      final byte[] bytes = new byte[bitMap[i] / 8];
      fcsFile.read(bytes);
      ByteBuffer buffy = ByteBuffer.wrap(bytes);
      
      //Swap bytes around based on endian type.
      if (endianness.equals(BYTE_ORDER_BIG)) {
        buffy = buffy.order(ByteOrder.BIG_ENDIAN);
      } else if (endianness.equals(BYTE_ORDER_LITTLE)) {
        buffy = buffy.order(ByteOrder.LITTLE_ENDIAN);
      } else if (endianness.contentEquals(BYTE_ORDER_MIX)   ) {
        //Create new temporary byte arrays.
        byte[] ba12 = new byte[bitMap[i] / 8/2];
        byte[] ba34 = new byte[bitMap[i] / 8/2];
        // Read bytes 3&4
        for (int byteIndex=0;byteIndex < ba12.length;byteIndex++) {
          ba34[byteIndex] = bytes[byteIndex];
        }
        // Read bytes 1&2
        for (int byteIndex=0;byteIndex < ba34.length;byteIndex++) {
          ba12[byteIndex] = bytes[byteIndex + bytes.length/2];
        }
        // Create and fill little endian array with byte fragmets 1,2 and 3,4
        byte[] littleE = new byte[bitMap[i] / 8];
        for (int byteIndex = 0;byteIndex<littleE.length;byteIndex++) {
          if (byteIndex < littleE.length/2) {
            //Add bytes from 1,2 to start.
            littleE[byteIndex] = ba12[byteIndex];
          } else {
            //Add bytes from 3,4 to 2nd half
            int offset = (littleE.length/2);
            littleE[byteIndex] = ba34[byteIndex-offset];
          }
        }
        // Byte order should now be 1,2,3,4 (LE)
        buffy = ByteBuffer.wrap(littleE);
        buffy.order(ByteOrder.LITTLE_ENDIAN);
      } else {
        throw new IOException("Unknown endian definition: " + endianness);
      }
      
      // Read data as specifcied datatype.
      if (DATATYPE_FLOAT.equals(dataType)) {
        row[i] = buffy.getFloat();
      } else if (DATATYPE_INT.equals(dataType)) {
        row[i] = buffy.getInt();
      } else {
        throw new IOException("DataType " + dataType + " not supported." );
      }
    }
    return row;
  }

  public static FCSFrame read(String filePath) {
    FCSFileReader reader;
    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")){
      reader = new FCSFileReader(filePath, raf);
      reader.readData();
      return reader.getFCSFrame();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Unable to read file.", e);
      return null;
    }
  }

  public static FCSFrame readNoData(String filePath) {
    FCSFileReader reader;
    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
      reader = new FCSFileReader(filePath,raf);
      reader.initializeFrame();
      return reader.getFCSFrame();
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Unable to read file.", e);
      return null;
    }
  }

  public static Map<String, String> readHeaderOnly(String filePath) {
    FCSFileReader reader;
    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
      reader = new FCSFileReader(filePath, raf);
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
    try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")){
      FCSFileReader reader = new FCSFileReader(filePath, raf);
      isValid = FCSUtilities.validateHeader(reader.getHeader());
    } catch (Exception e) {
      LOGGER.log(Level.FINE, "Invalid File:" + filePath, e);
      isValid = false;
    }
    return isValid;
  }
}
