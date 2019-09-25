package inflor.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fleur.core.data.FCSFrame;
import fleur.core.fcs.FCSFileReader;
import fleur.core.transforms.LogicleTransform;

public class MultiRegressorTree {
	String protoPath = "";
	@Test
	public void testInitialization() throws Exception {
		// Setup
		FCSFrame data = FCSFileReader.read(protoPath);
		LogicleTransform transform = new LogicleTransform(262144);
		double[] tData = transform.transform(data.getDimension("FSC-A").getData());
		// Test

		// Assert
		assertEquals(data.getDimension("FSC-A").getData().length, tData.length);

		// assertEquals( r.bitMap, new Integer[] {16,16});
		System.out.println("LogicleTransformTest::testInitialization completed.");

	}
}
