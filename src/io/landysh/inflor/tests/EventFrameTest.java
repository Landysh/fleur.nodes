package io.landysh.inflor.tests;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;

public class EventFrameTest {

	@Test
	public void testInitialization() throws Exception {
		// Setup
		final String trueKey = "key";
		final String trueValue = "value";
		final int 	 trueRowCount = 15;

		final HashMap<String, String> header = new HashMap<String, String>();
		header.put(trueKey, trueValue);
		
		// Test
		final ColumnStore cs = new ColumnStore(header, trueRowCount);
		final String testValue = cs.getKeywords().get(trueKey);
		final int testRowCount = cs.getRowCount(); 
		String testPrefName = cs.getPrefferedName();
		
		// Assert
		assertEquals("keyword", trueValue, testValue);
		assertEquals("rowcount", trueRowCount, testRowCount);
		assertEquals("preferredName", "Foo", testPrefName);
		System.out.println("EventFrameTest testInitialization completed (succefully or otherwise)");
	}
}