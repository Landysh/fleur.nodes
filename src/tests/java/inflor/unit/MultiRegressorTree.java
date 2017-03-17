package inflor.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import inflor.core.data.FCSFrame;
import inflor.core.fcs.FCSFileReader;
import inflor.core.transforms.LogicleTransform;

public class MultiRegressorTree {
	String protoPath = "";
	@Test
	public void testInitialization() throws Exception {
		// Setup
		FCSFrame data = FCSFileReader.read(protoPath);
		LogicleTransform transform = new LogicleTransform();
		double[] tData = transform.transform(data.getDimension("FSC-A").getData());
		// Test

		// Assert
		assertEquals(data.getDimension("FSC-A").getData().length, tData.length);

		// assertEquals( r.bitMap, new Integer[] {16,16});
		System.out.println("LogicleTransformTest::testInitialization completed.");

	}
}
