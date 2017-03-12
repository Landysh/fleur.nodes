package inflor.unit;

import org.junit.Test;

import inflor.core.data.Histogram2D;

import static org.junit.Assert.assertEquals;

public class Histogram2DTest {

  double[] xData = {0, 1, 0, 1, 2};
  double xMin = 0;
  double xMax = 3;
  int xBinCount = 3;

  double[] yData = {1, 2, 1, 0, 0};
  double yMin = 0;
  double yMax = 3;
  int yBinCount = 3;

  @Test
  public void testConstructor() throws Exception {
    // Setup
    Histogram2D testHitogram = new Histogram2D(xData, xMin, xMax, yData, yMin, yMax);
    double xWidthTruth = 1;
    double yWidthTruth = 1;

    // Test
    double xWidthTest = testHitogram.getXBinWidth();
    double yWidthTest = testHitogram.getYBinWidth();

    // Assert
    assertEquals("XBinWidth", xWidthTruth, xWidthTest, Double.MIN_VALUE);
    assertEquals("YBinWidth", yWidthTruth, yWidthTest, Double.MIN_VALUE);
    System.out.println("Histogram2DTest::testConstructor completed (succefully or otherwise)");
  }

  @Test
  public void testInitializeHistogram() throws Exception {
    // Setup
    Histogram2D testHistogram = new Histogram2D(xData, xMin, xMax, yData, yMin, yMax);

    double[] xBinsTruth = {0, 0, 0, 1, 1, 1, 2, 2, 2};
    double[] yBinsTruth = {0, 1, 2, 0, 1, 2, 0, 1, 2};

    // Test
    testHistogram.initializeHistogram(xBinCount, testHistogram.getXBinWidth(), xMin, yBinCount,
        testHistogram.getYBinWidth(), yMin);
    double[] xBinsTest = testHistogram.getXBins();
    double[] yBinsTest = testHistogram.getYBins();
    double[] zValuesTest = testHistogram.getZValues();

    // Assert

    for (int i = 0; i < xBinsTruth.length; i++) {
      assertEquals("xBin: " + i, xBinsTruth[i], xBinsTest[i], Double.MIN_VALUE);
      assertEquals("yBin: " + i, yBinsTruth[i], yBinsTest[i], Double.MIN_VALUE);
      assertEquals("zValue: " + i, 0., zValuesTest[i], Double.MIN_VALUE);
    }

    System.out
        .println("Histogram2DTest::testInitializeHistogram completed (succefully or otherwise)");
  }

  @Test
  public void testPopulateHistogram() {
    // Setup
    Histogram2D testHistogram = new Histogram2D(xData, xMin, xMax, yData, yMin, yMax);

    double[] initValues = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    double xWidthTruth = 1;
    double yWidthTruth = 1;

    double[] zValuesTruth = {0, 2, 0, 1, 0, 1, 1, 0, 0};

    // Test
    testHistogram.populateHistogram(initValues, xData, yData, xWidthTruth, yWidthTruth, yBinCount);
    double[] zValuesTest = testHistogram.getZValues();

    // Assert
    for (int i = 0; i < zValuesTruth.length; i++) {
      assertEquals("zValue: " + i, zValuesTruth[i], zValuesTest[i], Double.MIN_VALUE);
    }
    System.out
        .println("Histogram2DTest::testPopulateHistogram completed (succefully or otherwise)");

  }
}
