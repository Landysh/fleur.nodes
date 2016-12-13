package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;

public class EventFrameTest {

  @Test
  public void testInitialization() throws Exception {
    // Setup
    final String trueKey = "key";
    final String trueValue = "value";
    final int trueRowCount = 15;

    final HashMap<String, String> header = new HashMap<String, String>();
    header.put(trueKey, trueValue);

    // Test
    final FCSFrame cs = new FCSFrame(header, trueRowCount);
    final String testValue = cs.getKeywords().get(trueKey);
    final int testRowCount = cs.getRowCount();
    String testPrefName = cs.getPrefferedName();

    // Assert
    assertEquals("keyword", trueValue, testValue);
    assertEquals("rowcount", trueRowCount, testRowCount);
    assertEquals("preferredName", "Foo", testPrefName);
    System.out.println("EventFrameTest testInitialization completed (succefully or otherwise)");
  }
}
