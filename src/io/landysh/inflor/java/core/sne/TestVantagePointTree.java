package io.landysh.inflor.java.core.sne;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestVantagePointTree {

	@Test
	public void testInitialization() throws Exception {
		// Setup
		double[][] data = {{0.,0.},
				   {1.,1.},
				   {2.,2.},
				   {3.,3.},
				   {4.,4.},
				   {5.,5.}};		
		
		// Test

		VantagePointTree tree = new VantagePointTree(data);

		// Assert
		assertEquals("Initialized", data, tree.getData());
		System.out.println("TestVantagePointTree::testInitialization completed (succefully or otherwise)");
	}
}
