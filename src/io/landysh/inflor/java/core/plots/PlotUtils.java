package io.landysh.inflor.java.core.plots;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;

import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicTransform;
import io.landysh.inflor.java.core.transforms.TransformType;

public class PlotUtils {

	public static ValueAxis createAxis(String Name, AbstractTransform transform) {
		NumberAxis axis = new NumberAxis(Name);
		if (transform instanceof BoundDisplayTransform){
			BoundDisplayTransform bdt = (BoundDisplayTransform) transform;
			axis.setRange(new Range(bdt.getMinValue(),bdt.getMaxValue()));
			axis.setTickMarkInsideLength(0);
			axis.setTickMarkOutsideLength(2);
			return axis;
		} else if (transform instanceof LogicleTransform){
			LogicleTransform llt = (LogicleTransform) transform;
			axis.setRange(new Range(llt.getMinValue(),llt.getMaxValue()));
			axis.setTickMarkInsideLength(0);
			axis.setTickMarkOutsideLength(2);
			return axis;
		} else if (transform instanceof LogrithmicTransform){
			LogrithmicTransform logTransform = (LogrithmicTransform) transform;
			axis.setRange(new Range(logTransform.getMin(), logTransform.getMax()));
			axis.setTickMarkInsideLength(0);
			axis.setTickMarkOutsideLength(2);
			return axis;
		} else {
			throw new RuntimeException("Transformation type not supported. Yet.");
		}
	}

	public static AbstractFCChart createPlot(ChartSpec plotSpec) {
		PlotTypes type = plotSpec.getPlotType();
		AbstractFCChart newPlot = null;		
		if (type.equals(PlotTypes.Contour)){
			newPlot = new DensityPlot(plotSpec);
		} else if (type.equals(PlotTypes.Scatter)){
			DensityPlot grayScalePlot = new DensityPlot(plotSpec);
			grayScalePlot.updateColorScheme(ColorSchemes.GRAY_SCALE);
			newPlot = grayScalePlot;
		} else {
			throw new RuntimeException("No valid plot type selected.");
		}
		return newPlot;
	}

	public static AbstractTransform createDefaultTransform(TransformType selectedType) {
		
		AbstractTransform newTransform; 
		
		if (selectedType == TransformType.Linear){
			newTransform = new BoundDisplayTransform(0, 262144);
		 } else if (selectedType == TransformType.Bounded){
				newTransform = new BoundDisplayTransform(0, 262144);

		 } else if (selectedType == TransformType.Logrithmic){
				newTransform = new LogrithmicTransform(100, 262144);

		 } else if (selectedType == TransformType.Logicle){
			 newTransform = new LogicleTransform();
		 } else {
			 //TODO: No use case?
			 newTransform = null;
		 }
		return newTransform;
	}
}
