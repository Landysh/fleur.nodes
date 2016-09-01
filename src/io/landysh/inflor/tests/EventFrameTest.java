package io.landysh.inflor.tests;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import org.junit.Test;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;

public class EventFrameTest {

	@Test
	public void testInitialization() throws Exception {
		// Setup
		final String testKey = "key";
		final String testVal = "value";
		final String column1 = "c1";
		final String column2 = "c2";

		final Hashtable<String, String> header = new Hashtable<String, String>();
		header.put(testKey, testVal);
		// Test
		final ColumnStore cs = new ColumnStore(header, new String[] { column1, column2 });

		final boolean contains1 = cs.getData().keySet().contains(column1);
		final boolean contains2 = cs.getData().keySet().contains(column2);

		// Assert
		assertEquals("My First Message", testVal, cs.getKeywords().get(testKey));
		assertEquals("My Second Message", contains1, true);
		assertEquals("My Third Message", contains2, true);
		System.out.println("EventFrameTest testInitialization completed (succefully or otherwise)");
	}
}