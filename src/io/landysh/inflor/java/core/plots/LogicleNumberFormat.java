package io.landysh.inflor.java.core.plots;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.transforms.TickFormatter;

@SuppressWarnings("serial")
public class LogicleNumberFormat extends NumberFormat {
	
	private LogicleTransform transform;

	public LogicleNumberFormat(LogicleTransform transform) {
		this.transform = transform;
	}
	
	@Override
	public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2) {
		//double[] tickPositions = transform.getAxisValues();
		double val = transform.inverse(arg0);
		int iVal = (int)Math.round(val);
		
						
		String tickLabel = null;
		if (Math.abs(iVal)<=990)
		{	
			tickLabel = Integer.toString(iVal);
		} 
		else
		{
			tickLabel =  TickFormatter.findLogTick(iVal);
		}
		StringBuffer buffer = new StringBuffer(tickLabel);
		return buffer;
	}

	@Override
	public StringBuffer format(long arg0, StringBuffer arg1, FieldPosition arg2) 
	{
		// TODO: needed?
		return null;
	}

	@Override
	public Number parse(String arg0, ParsePosition arg1) 
	{
		double d = Double.parseDouble(arg0);
		double number = transform.transform(d);
		return number;
	}
}