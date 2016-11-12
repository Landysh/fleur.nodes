package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

// TestDependencies
import io.landysh.inflor.java.core.dataStructures.FCSFrame;
// Class we are testing.
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.transforms.LogicleTransform;

public class LogicleTransformTest {
  // Define Constants

  String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";

  @Test
  public void testInitialization() throws Exception {
    // Setup
    FCSFrame data = FCSFileReader.read(logiclePath);
    LogicleTransform transform = new LogicleTransform();
    double[] tData = transform.transform(data.getFCSDimensionByShortName("FSC-A").getData());
    // Test

    // Assert
    assertEquals(data.getFCSDimensionByShortName("FSC-A").getData().length, tData.length);

    // assertEquals( r.bitMap, new Integer[] {16,16});
    System.out.println("LogicleTransformTest::testInitialization completed.");

  }
}
