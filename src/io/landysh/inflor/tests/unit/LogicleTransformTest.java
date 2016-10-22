package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//TestDependencies
import io.landysh.inflor.java.core.dataStructures.ColumnStore;

//Class we are testing.
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.transforms.LogicleTransform;

public class LogicleTransformTest {
	// Define Constants

	String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
	
	@Test
	public void testInitialization() throws Exception {
		// Setup
		ColumnStore data = FCSFileReader.read(logiclePath, false);
		LogicleTransform transform = new LogicleTransform();
		double[] axes = transform.getAxisValues();
		double[] tData = transform.transform(data.getDimensionData("FSC-A"));
		// Test

		// Assert
		assertEquals(axes, axes);

		// assertEquals( r.bitMap, new Integer[] {16,16});
		System.out.println("LogicleTransformTest::testInitialization completed.");

	}
}