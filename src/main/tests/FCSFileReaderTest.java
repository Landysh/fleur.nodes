package main.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Hashtable;

import main.java.FCSFileReader;

public class FCSFileReaderTest {
	//Define Constants  
//	private static final int LOWER_BOUND = 1000;
	  
	String path1 = "src/main/tests/extData/int-15_scatter_events.fcs";
	@SuppressWarnings("deprecation")
	@Test
	  public void testInitialization() throws Exception {
		//Setup
		FCSFileReader r = new FCSFileReader(path1);
		 
		//Test
		
	    //Assert
	    assertEquals(r.pathToFile,	path1);
	    assertEquals( r.beginText, 	(Integer) 256);
	    assertEquals( r.endText,   	(Integer) 511);
	    assertEquals( r.beginData, 	(Integer) 576);
	    assertEquals( r.endData, 	(Integer) 635);
	    assertEquals( r.dataType, 	"I");
	    assertEquals( r.bitMap, 	new Integer[] {16,16});
	    System.out.println("Test1 completed (succefully or otherwise)");

	  }
	 @Test
	  public void testReadAllData() throws Exception {
		//Setup
		FCSFileReader r = new FCSFileReader(path1);
		
		//Test
	     Hashtable<String, double[]> testData = r.getColumnStore();
	     
	     double[] fcs = {400, 600, 300, 500, 600, 500, 800, 200, 300, 800, 900, 400, 200, 600, 400};
	     double[] ssc = {300, 300, 600, 200, 800, 500, 600, 400, 100, 200, 400, 800, 900, 700, 500};
	     
	    //Assert
	    assertEquals( testData.get("FCS"), fcs );
	    assertEquals( testData.get("SSC"), ssc );
	    System.out.println("Test2 completed (succefully or otherwise)");
	}
}