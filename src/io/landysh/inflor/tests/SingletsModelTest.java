package io.landysh.inflor.tests;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import io.landysh.inflor.java.core.SingletsModel;

public class SingletsModelTest {
	//Define Constants  

	//Case 1
	String[] testParameters1		= {"FL3-H","FL2-A","FL2-H","FL13-H","FL1-A","Time","FL12-A","FL1-H","FSC-Width","FL12-H","SSC-A","FL13-A","FL11-A","SSC-H","FL11-H","FSC-A","FL10-A","FSC-H","FL10-H","FL8-H","FL9-A","FL7-A","FL9-H","FL7-H","FL8-A","FL6-A","FL6-H","FL5-A","FL5-H","FL4-A","FL4-H","FL3-A"};
	String[] usableColumns1 		= {"FL3-H","FL2-A","FL2-H","FL13-H","FL1-A","FL12-A","FL1-H","FSC-Width","FL12-H","SSC-A","FL13-A","FL11-A","SSC-H","FL11-H","FSC-A","FL10-A","FSC-H","FL10-H","FL8-H","FL9-A","FL7-A","FL9-H","FL7-H","FL8-A","FL6-A","FL6-H","FL5-A","FL5-H","FL4-A","FL4-H","FL3-A"};
	String[] unUsableColumns1 		= {"Time"};
	String[][] result1VectorSet1 	= {
									    {"FSC-A", "FSC-H", "FSC-Width"},
									    {"SSC-A",  "SSC-H"},
									    {"FL1-A",  "FL1-H"},
									    {"FL2-A",  "FL2-H"}, 
									    {"FL3-A",  "FL3-H"}, 
									    {"FL4-A",  "FL4-H"},
									    {"FL5-A",  "FL5-H"},
									    {"FL6-A",  "FL6-H"},
									    {"FL7-A",  "FL7-H"},
									    {"FL8-A",  "FL8-H"},
									    {"FL9-A",  "FL9-H"},
									    {"FL10-A", "FL10-H"},
									    {"FL11-A", "FL11-H"},
									    {"FL12-A", "FL12-H"},
									    {"FL13-A", "FL13-H"}
									  };
	
	//Case 2
	String[] 	testParameters2 	= {"CD3 CD14 Live/Dead-A","PE-Cy7-A","PE-Cy55-A","PE-A","FITC/Alexa 488-A","SSC-W","SSC-H","SSC-A","FSC-W","Time","FSC-H","APC-H7-A","FSC-A","APC-Ax700-A","APC/Ax 647-A"};
	String[] 	usableColumns2 		= {"SSC-W","SSC-H","SSC-A","FSC-W","FSC-H","FSC-A"};
	String[] 	unUsableColumns2 	= {"CD3 CD14 Live/Dead-A","PE-Cy7-A","PE-Cy55-A","PE-A","FITC/Alexa 488-A","SSC-W","SSC-H","SSC-A","FSC-W","Time","FSC-H","APC-H7-A","FSC-A","APC-Ax700-A","APC/Ax 647-A"};
	String[][] 	result1VectorSet2	= {
									    {"SSC-W", "SSC-H", "SSC-A"},
									    {"FSC-W","FSC-H","FSC-A"}
									  };
	
	
	//Case 3
	String[] 	testParameters3 	= {"FSC-A","APC-A","PE-Cy7-A","PerCP-A","PE-A","FITC-A","Time","SSC-A","APC-Cy7-A"};
	String[] 	usableColumns3 		= {};
	String[] 	unUsableColumns3 	= {"FSC-A","APC-A","PE-Cy7-A","PerCP-A","PE-A","FITC-A","Time","SSC-A","APC-Cy7-A"};
	String[][]	result1VectorSet3  	= {{}};

	//Now Wrap them all up.
	String [][] 	usableColumns = {usableColumns1,usableColumns2,usableColumns3};
	String [][] 	unUsableColumns = {unUsableColumns1,unUsableColumns2,unUsableColumns3};
	String[][][] 	vectorSets = {result1VectorSet1,result1VectorSet2,result1VectorSet3};
	




	
	@Test
	public void testFindColumnNames() throws Exception {
		//Setup.  Reference future new cases here.
		SingletsModel model1 = new SingletsModel(testParameters1);
		SingletsModel model2 = new SingletsModel(testParameters2);
		SingletsModel model3 = new SingletsModel(testParameters3);
		
		SingletsModel[] testModels = {model1, model2, model3};

		//Test the defined cases. 
		for (int i=0;i<testModels.length;i++){
			SingletsModel model = testModels[i];
			String[] usables = usableColumns[i];
			String[] unUsables = unUsableColumns[i];
			for (String current: model.getSingletsParameters()){
				boolean inUsable = false;
				for (String usable: usables){
					if (usable.equals(current)){
						inUsable = true;
					}
				}
				boolean inUnUsable = false;
				for (String unUsable: unUsables){
					if (unUsable.equals(current)){
						inUsable = true;
					}
	
				}
				assertTrue(inUsable);
				assertTrue(!inUnUsable);
			}
			System.out.println("SingletsModel testFindColumnNames completed (succefully or otherwise)");

		}			
	}
	
	public void testCreateVectorPairs() {
		

	
		
	}
}