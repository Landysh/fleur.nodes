package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.fcs.FCSFileReader;
import io.landysh.inflor.main.core.transforms.LogicleTransform;

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
