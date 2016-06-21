package io.landysh.inflor.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventFrameTest {
	//Define Constants  
	  
	String path1 = "src/io/landysh/inflor/tests/extData/int-15_scatter_events.fcs";

	@Test
	public void testInitialization() throws Exception {
	//Setup

		//Test

	    //Assert
		assertEquals(1.,1., 0.);
	    System.out.println("EventFrameTest testInitialization completed (succefully or otherwise)");

	  }
	 
	@Test
	public void testParseSpillover() throws Exception {
		//Setup
		double[][] truespillover = new double[][] 
			{
			{1,0.16643300787482143,0.009606886236148684,0.0015051009909208169,0,2.0518888830885058E-4,0,0},
			{0.007384218964039767,1,0.11687207968748373,0.023480652355636036,8.504755266136914E-5,7.224623180112922E-4,6.267928046597421E-4,7.114916490938666E-4},
			{2.5139760025828523E-4,2.5126988754534075E-4,1,0.3742602095972257,0,0.049038905774422256,0.7652805220707191,0.08902127783467159},
			{8.061934915472157E-4,0.008544464271790835,0.002942178984734687,1,0,0.08605684505229083,0.10910662609106951,0.10497947698872584},
			{0.00536910186265687,0.0017894617892055265,0,0,1,0,0,0},
			{9.545641530928031E-5,0,0.0026954618627686245,6.188017905481432E-4,0,1,1.0535114953329914,0.08876670472835832},
			{2.478312534206834E-4,1.4162863644431127E-4,0.002880929008212223,0.00241808805664468,0,0.0025690168540350327,1,0.07873420636831327},
			{8.340404224078871E-4,0,1.2356486776429765E-4,0.017113326321679487,3.9026099367006574E-4,0.05145246481613921,0.17931761751577005,1}
			};
		
//		FCSFileReader file1 = new FCSFileReader(path1);
//		Hashtable <String, String> header1 = file1.getHeader();
//		ColumnStore frame = new ColumnStore(header1);
//		
//		//Test 
//		double[][] testSpillover = frame.FCSpillMatrix;
//	    
//		//Assert    
//		for (int i=0; i<truespillover.length; i++){
//			for (int j=0; j<truespillover.length; j++){
//				assert(truespillover[i][j] == testSpillover[i][j]);
//		   }
//	   }
	   System.out.println("EventFrameTest testSpillover completed (succefully or otherwise)");
	}
}