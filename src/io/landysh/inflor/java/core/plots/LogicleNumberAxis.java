package io.landysh.inflor.java.core.plots;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import io.landysh.inflor.java.core.transforms.LogicleTransform;

@SuppressWarnings("serial")
public class LogicleNumberAxis extends NumberAxis {
	
	private LogicleTransform logicle;

	public LogicleNumberAxis(String name, LogicleTransform transform){
		super(name);
		this.logicle = transform;
		this.setRange(new Range(transform.getMinTranformedValue(),transform.getMaxTransformedValue()));
		NumberFormat formatter = new LogicleNumberFormat(transform);
		this.setNumberFormatOverride(formatter);
		this.setTickMarkOutsideLength(2);
	}
	
	@Override
	public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge){
		List<NumberTick> ticks = new ArrayList<NumberTick>();
		for (double d: logicle.getAxisValues()){
			double td = logicle.transform(d);
			String label = this.getNumberFormatOverride().format(td);
			NumberTick tick = new NumberTick(td, label, TextAnchor.CENTER, TextAnchor.CENTER, 0);
			ticks.add(tick);
		}
		return ticks;
	}
}
