package io.landysh.inflor.java.core.plots;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class CategoryNumberFormat extends NumberFormat {
	
	private Map<Integer, String> labelMap;

	public CategoryNumberFormat(Map<Integer, String> labelMap) 
	{
		this.labelMap = labelMap;
	}
	
	@Override
	public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2) 
	{
		StringBuffer buffer = new StringBuffer(labelMap.get((int) arg0));
		return buffer;
	}

	@Override
	public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number parse(String arg0, ParsePosition arg1) 
	{
		for (Entry<Integer, String> e: labelMap.entrySet()){
			if (e.getValue()==arg0){
				return e.getKey();
			}
		}
		return null;
	}
}
