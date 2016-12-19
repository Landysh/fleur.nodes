package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.BitSet;

import org.junit.Test;

import io.landysh.inflor.main.core.data.FCSDimension;
import io.landysh.inflor.main.core.data.FCSFrame;
import io.landysh.inflor.main.core.gates.PolygonGate;

public class PolygonGateTest {

  String id = "1";
  String d1Name = "d1";
  double[] d1Values = {0., 0., 1., 1.};
  String d2Name = "d2";
  double[] d2Values = {0., 1., 1., 0.};


  @Test
  public void testEvaluate() throws Exception {
    // Setup


    FCSFrame testData = new FCSFrame();
    FCSDimension d1 = new FCSDimension(1, 0, d1Name, d1Name, 0, 0, 0);
    d1.getData()[0] = 0.5;

    FCSDimension d2 = new FCSDimension(1, 0, d2Name, d2Name, 0, 0, 0);
    d2.getData()[0] = 0.5;
    testData.addDimension(d1);
    testData.addDimension(d2);

    // Test
    final PolygonGate testGate = new PolygonGate("", d1Name, d1Values, d2Name, d2Values);
    testGate.validate();
    final BitSet result = testGate.evaluate(testData);
    // Assert
    assertEquals("Evaluated", result.get(0), true);
    System.out.println("EventFrameTest::testEvaluate completed (succefully or otherwise)");
  }

  @Test
  public void testInitialization() throws Exception {
    // Setup
    String id = "1";

    // Test
    PolygonGate testGate = new PolygonGate("", d1Name, d1Values, d2Name, d2Values);

    // Assert
    assertEquals("Initialized", id, testGate.getID());
    System.out.println("PolygonGateTest::testInitialization completed (succefully or otherwise)");
  }

  @Test
  public void testValidate() throws Exception {

    ArrayList<String> trueNames = new ArrayList<String>();
    trueNames.add(d1Name);
    trueNames.add(d2Name);

    // Test
    PolygonGate testGate = new PolygonGate("", d1Name, d1Values, d2Name, d2Values);

    testGate.validate();

    final String[] testNames = testGate.getDimensionNames();
    final double[] testVertex = testGate.getVertex(0);

    // Assert
    assertEquals("Initialized", testNames, trueNames);
    assertEquals("Validated", testVertex, new Double[] {0., 0.});
    System.out.println("EventFrameTest::testValidate completed (succefully or otherwise)");
  }
}
