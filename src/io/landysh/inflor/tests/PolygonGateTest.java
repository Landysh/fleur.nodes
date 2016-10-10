package io.landysh.inflor.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import org.junit.Test;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.dataStructures.FCSDimension;
import io.landysh.inflor.java.core.gatingML.gates.polygonGate.PolygonGate;

public class PolygonGateTest {

	@Test
	public void testEvaluate() throws Exception {
		// Setup
		final String id = "1";

		final String d1Name = "d1";
		final Double[] d1Values = { 0., 0., 1., 1. };

		final String d2Name = "d2";
		final Double[] d2Values = { 0., 1., 1., 0. };

		final ColumnStore testData = new ColumnStore();
		FCSDimension d1 = new FCSDimension(1, 0, d1Name, d1Name, 0, 0, 0, false);
		d1.getData()[0] = 0.5;
		
		FCSDimension d2 = new FCSDimension(1, 0, d2Name, d2Name, 0, 0, 0, false);
		d2.getData()[0] = 0.5;
		testData.addColumn(d1Name, d1);
		testData.addColumn(d2Name, d2);

		// Test
		final PolygonGate testGate = new PolygonGate(id);

		testGate.setDimensions(d1Name, d2Name);
		testGate.setPoints(new ArrayList<Double>(Arrays.asList(d1Values)),
				new ArrayList<Double>(Arrays.asList(d2Values)));

		testGate.validate();

		final BitSet result = testGate.evaluate(testData);

		// Assert
		assertEquals("Evaluated", result.get(0), true);
		System.out.println("EventFrameTest::testEvaluate completed (succefully or otherwise)");
	}

	@Test
	public void testInitialization() throws Exception {
		// Setup
		final String id = "1";

		// Test
		final PolygonGate testGate = new PolygonGate(id);

		// Assert
		assertEquals("Initialized", id, testGate.getId());
		System.out.println("PolygonGateTest::testInitialization completed (succefully or otherwise)");
	}

	@Test
	public void testValidate() throws Exception {
		// Setup
		final String id = "1";

		final String d1Name = "d1";
		final Double[] d1Values = { 0., 0., 1., 1. };

		final String d2Name = "d2";
		final Double[] d2Values = { 0., 1., 0., 1. };

		final ArrayList<String> trueNames = new ArrayList<String>();
		trueNames.add(d1Name);
		trueNames.add(d2Name);

		// Test
		final PolygonGate testGate = new PolygonGate(id);

		testGate.setDimensions(d1Name, d2Name);
		testGate.setPoints(new ArrayList<Double>(Arrays.asList(d1Values)),
				new ArrayList<Double>(Arrays.asList(d2Values)));

		testGate.validate();

		final ArrayList<String> testNames = testGate.getDimensionNames();
		final Double[] testVertex = testGate.getVertex(0);

		// Assert
		assertEquals("Initialized", testNames, trueNames);
		assertEquals("Validated", testVertex, new Double[] { 0., 0. });
		System.out.println("EventFrameTest::testValidate completed (succefully or otherwise)");
	}
}