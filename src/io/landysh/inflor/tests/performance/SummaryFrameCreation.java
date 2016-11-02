package io.landysh.inflor.tests.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.utils.FCSUtils;

public class SummaryFrameCreation {
  static final int numFiles = 10;
  ArrayList<FCSFrame> dataSet = new ArrayList<FCSFrame>();

  public static void main(String[] args) throws Exception {
    // Setup data
    String dirPath = "C:\\Users\\Aaron\\Desktop\\inflor test cases\\Panel_1";


    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<String>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath) == true) {
        validFiles.add(filePath);
      } else if (file.isDirectory()) {
        System.out.println("Directory " + file.getName());
      }
    }


    HashMap<String, FCSDimension> dataset = new HashMap<String, FCSDimension>();
    System.out.println("Reading data started");

    List<FCSFrame> fcsList = validFiles.parallelStream()
        .map(filePath -> FCSFileReader.read(filePath)).collect(Collectors.toList());
    System.out.println("Reading data finished");
    Thread.sleep(2000);

    long start = System.currentTimeMillis();
    
    FCSFrame summaryFrame = FCSUtils.createSummaryFrame(fcsList, 10000);
    
    long end = System.currentTimeMillis();
    System.out.println("Millis for a stream: " + (end - start));

  }

}
