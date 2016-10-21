package io.landysh.inflor.tests.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.landysh.inflor.java.core.dataStructures.ColumnStore;
import io.landysh.inflor.java.core.fcs.FCSFileReader;

public class FileReading {
	static final int numFiles = 10;
	ArrayList<ColumnStore> dataSet = new ArrayList<ColumnStore>();
	
	public static void main(String[] args) throws Exception {
		String path = "src/io/landysh/inflor/tests/extData/logicle-example.fcs";
		ArrayList<String> filePaths = new ArrayList<String>();
		for (int i=0;i<numFiles;i++){
			filePaths.add(path);
		}
		
		long start = System.currentTimeMillis();
		List<ColumnStore> list = filePaths.stream().map(filePath -> FCSFileReader.read(filePath, false)).collect(Collectors.toList());
		long end = System.currentTimeMillis(); 
		System.out.println("Millis for a stream: " + (end-start));
		list.clear();
		Thread.sleep(2000);
		start = System.currentTimeMillis();
		list = filePaths.parallelStream().map(filePath -> FCSFileReader.read(filePath, false)).collect(Collectors.toList());
		end = System.currentTimeMillis();		
		System.out.println("Millis for a parallel stream: " + (end-start));
		list.clear();
		Thread.sleep(2000);
		ArrayList<ColumnStore> data = new ArrayList<>();
		start = System.currentTimeMillis();
		filePaths.parallelStream().map(filePath -> FCSFileReader.read(filePath, false)).forEach(store -> data.add(store));
		end = System.currentTimeMillis();		
		System.out.println("Millis for a parallel stream terminal foreach: " + (end-start));
		list.clear();
		
	}	
	
}
