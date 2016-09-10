package io.landysh.inflor.java.core.plots;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;

import io.landysh.inflor.java.core.transforms.LogicleTransform;

@SuppressWarnings("serial")
public class LogicleAxis extends NumberAxis {
	
	private LogicleTransform transform;
	
	protected final NumberFormat numberFormatterObj = NumberFormat.getInstance();
	
	LogicleAxis(String label, LogicleTransform transform){
		super(label);
		this.transform = transform;
		initializeNumberFormat();
	}

	private void initializeNumberFormat() {
		((DecimalFormat)this.numberFormatterObj).applyPattern("0E0");
	}
	
	public double transformValue(double value){
		double transformedValue = transform.transform(value);
		return transformedValue;
	}
	
	public double inverseValue(double value){
		double invertedValue = transform.inverse(value);
		return invertedValue;
	}
	
    @Override
    public void setRange(Range range) {
        super.setRange(range);
    }
    
    /**
     * Rescales the axis to ensure that all data is visible.
     */
    @Override
    public void autoAdjustRange() {

        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data.
        }
          setRange(new Range(0, 262144), false, false);
    }
}
