package tests.java.inflor.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.landysh.inflor.main.core.transforms.LogicleTransform;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.fcs.FCSFileReader;

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
