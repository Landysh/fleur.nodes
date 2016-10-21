package io.landysh.inflor.tests.performance;

import java.util.ArrayList;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RangeGate;
import io.landysh.inflor.java.core.gatingML.gates.rangeGate.RectangleGate;

public class RangeGateCalculation {
	static final int numFiles = 1;
	ArrayList<ColumnStore> dataSet = new ArrayList<ColumnStore>();
	
	public static void main(String[] args) throws Exception {
		String path = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		String bigPath = "src/io/landysh/inflor/tests/extData/20mbFCS3.fcs";

		RangeGate rangeGate = new RangeGate("Foo", new String[] {"FSC-A", "SSC-A"}, new double[] {40,000, 60,000}, new double[]{5000,10000});
		
		ColumnStore data = FCSFileReader.read(bigPath, false);
		//gate.evaluateParallel(data);
		long start = System.currentTimeMillis();
		for (int i=0;i<numFiles;i++){
			rangeGate.evaluate(data);
		}
		long end = System.currentTimeMillis(); 
		System.out.println("Millis: " + (end-start));
		
		RectangleGate rectGate = new RectangleGate("", "FSC-A", 40000, 60000,  "SSC-A", 5000, 10000);
		start = System.currentTimeMillis();
		for (int i=0;i<numFiles;i++){
			rectGate.evaluate(data);
		}
		end = System.currentTimeMillis(); 
		System.out.println("Millis rectangle: " + (end-start));		
	}	
}
