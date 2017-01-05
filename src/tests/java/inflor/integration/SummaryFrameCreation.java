package tests.java.inflor.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;
import main.java.inflor.core.utils.FCSUtilities;

public class SummaryFrameCreation {
  static final int FILE_COUNT = 10;
  ArrayList<FCSFrame> dataSet = new ArrayList<FCSFrame>();

  public static void main(String[] args) throws Exception {
    // Setup data
    String dirPath = "C:\\Users\\Aaron\\Desktop\\inflor test cases\\Panel_1";


    final File folder = new File(dirPath);
    final File[] files = folder.listFiles();
    final ArrayList<String> validFiles = new ArrayList<>();
    for (final File file : files) {
      final String filePath = file.getAbsolutePath();
      if (FCSFileReader.isValidFCS(filePath)) {
        validFiles.add(filePath);
      } else if (file.isDirectory()) {
        System.out.println("Directory " + file.getName());
      }
    }

    System.out.println("Reading data started");

    List<FCSFrame> fcsList = validFiles.parallelStream()
        .map(filePath -> FCSFileReader.read(filePath)).collect(Collectors.toList());
    System.out.println("Reading data finished");
    Thread.sleep(2000);

    long start = System.currentTimeMillis();
    
    @SuppressWarnings("unused")
    FCSFrame summaryFrame = FCSUtilities.createSummaryFrame(fcsList, 10000);
    
    long end = System.currentTimeMillis();
    System.out.println("Millis for a stream: " + (end - start));

  }

}
