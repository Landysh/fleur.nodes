package io.landysh.inflor.tests.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

//TestDependencies
import io.landysh.inflor.java.core.dataStructures.FCSFrame;

//Class we are testing.
import io.landysh.inflor.java.core.fcs.FCSFileReader;

public class FCSFileReaderTest {
	// Define Constants

	String path1 = "src/io/landysh/inflor/tests/extData/int-15_scatter_events.fcs";
	String logiclePath = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
	
	@Test
	public void testInitialization() throws Exception {
		// Setup
		final FCSFileReader r = new FCSFileReader(path1);

		// Test

		// Assert
		assertEquals(r.pathToFile, path1);
		assertEquals(r.beginText, (Integer) 256);
		assertEquals(r.endText, (Integer) 511);
		assertEquals(r.beginData, (Integer) 576);
		assertEquals(r.dataType, "I");
		// assertEquals( r.bitMap, new Integer[] {16,16});
		System.out.println("FCSFileReaderTest testInitialization completed.");

	}

	@Test
	public void testReadAllData15ScatterEvents() throws Exception {
		// Setup
		final FCSFileReader reader = new FCSFileReader(path1);

		// Test
		reader.readData();
		final FCSFrame dataStore = reader.getColumnStore();

		final double[] fcs = {400,600,300,500,600,500,800,200,300,800,900,400,200,600,400};
		final double[] ssc = {300,300,600,200,800,500,600,400,100,200,400,800,900,700,500};
		
		double[] testFCS = dataStore.getDimensionData("FCS");
		double[] testSSC = dataStore.getDimensionData("SSC");

		// Assert
		for (int i = 0; i < fcs.length; i++) {
			assertEquals(fcs[i], testFCS[i], Double.MIN_VALUE);
			assertEquals(ssc[i], testSSC[i], Double.MIN_VALUE);
		}
		System.out.println("FCSFileReaderTes::testReadAllData15ScatterEvents completed.");
		
	}
	
	@Test
	public void testReadAllLogicleDataNoComp() throws Exception {
		// Setup
		int trueRowCount = 30000;
		final double[] trueFSCFirstFew = {400,600,300,500,600,500,800,200,300,800,900,400,200,600,400};
		final double[] trueSSCFirstFew = {300,300,600,200,800,500,600,400,100,200,400,800,900,700,500};
		
		// Test
		final FCSFileReader reader = new FCSFileReader(logiclePath);
		reader.readData();
		final FCSFrame dataStore = reader.getColumnStore();
		final int testRowCount = dataStore.getRowCount();

		
		double[] testFCS = dataStore.getDimensionData("FSC-A");
		double[] testSSC = dataStore.getDimensionData("SSC-A");

		// Assert
		assertEquals("Row count", trueRowCount, testRowCount);
		for (int i = 0; i < trueFSCFirstFew.length; i++) {
			assertEquals(trueFSCFirstFew[i], testFCS[i], Double.MIN_VALUE);
			assertEquals(trueSSCFirstFew[i], testSSC[i], Double.MIN_VALUE);
		}
		System.out.println("FCSFileReaderTes::testReadAllLogicleDataNoComp completed.");
	}
}