package main.tests;

import static org.junit.Assert.assertEquals;

import java.io.RandomAccessFile;
import java.util.Hashtable;

import main.java.FCSFileReader;
import org.junit.Test;

public class FCSFileReaderTest {
	//Define Constants  
//	private static final int LOWER_BOUND = 1000;
	  @SuppressWarnings("deprecation")
	@Test
	  public void testInitialization() throws Exception {
		//Setup
		FCSFileReader r = new FCSFileReader("extData/int-15_scatter_events.fcs");
		
		//Test
		
	    //Assert
	    assertEquals( r.pathToFile, "extData/int-15_scatter_events.fcs");
	    assertEquals( r.FCSVersion, "FCS2.0");
	    assertEquals( r.beginText, 	(Integer) 256);
	    assertEquals( r.endText,   	(Integer) 511);
	    assertEquals( r.beginData, 	(Integer) 576);
	    assertEquals( r.endData, 	(Integer) 635);
	    assertEquals( r.dataType, 	"I");
	    assertEquals( r.bitMap, 	new Integer[] {16,16});
	  }
	  
	  public void testReadAllData() {
		//Setup
		  FCSFileReader r = new FCSFileReader("extData/int-15_scatter_events.fcs");
		
		//Test
	     double[][] data = r.readAllData();
	    
	     double[][] trueData = {{},{}};
	     
	    //Assert
	    assertEquals( data, 1 );
	}
}