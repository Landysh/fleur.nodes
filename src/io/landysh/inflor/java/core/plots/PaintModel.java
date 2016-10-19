package io.landysh.inflor.java.core.plots;

import java.awt.Color;
import java.awt.Paint;

public class PaintModel {
	
	double[] discreteData;
	double[] levelValues;
	Paint[] paint;
	private int levels;
	private double zMin;
	private double zMax;	
	
	public PaintModel(double[] zValues) {
        
		zMin = Double.MAX_VALUE;
		zMax = Double.MIN_VALUE;
		for (double d:zValues){
			if(d>zMax){zMax = d;}
			if(d<zMin){zMin = d;}
		}
		
		levels = (int) zMax;
		paint = createPaintArray(levels);
		discreteData = discretizeData(zValues, levels);

		
	}

	private double[] discretizeData(double[] zValues, int levels) {
		
		levelValues = new double[levels];
		for (int i=0;i<levels;i++){
			levelValues[i] = i;
		}
		
		double[] discreteValues = new double[zValues.length];
		for (int i=0;i<discreteValues.length;i++){
			if (zValues[i]>=levels){
				discreteValues[i] = levels;
			} else {
				discreteValues[i] = zValues[i] + zMin;
			}
		}
		
		return discreteValues;
	}

	private Paint[] createPaintArray(int levels) {
		
		Paint[] colorScale = new Paint[levels];
		float startH = 200/360f;
		float deltaH = startH/levels;
		
		for (int i=0;i<colorScale.length;i++){
			float hue = startH-(i)*deltaH;
			colorScale[i] = Color.getHSBColor(hue, 0.7f, 1f);
		}
		
		return colorScale;
	}

	public double[] getDiscreteData(double[] z) {
		return discretizeData(z, levels);
	}

	public Paint[] getPaints() {
		return paint;
	}

	public double[] getLevels() {
		return levelValues;
	}

	public double getThreshold() {
		return levels;
	}

}
