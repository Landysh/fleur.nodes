package io.landysh.inflor.java.core.dataStructures;

import io.landysh.inflor.java.core.fcs.ParameterTypes;
import io.landysh.inflor.java.core.plots.ChartingDefaults;
import io.landysh.inflor.java.core.transforms.AbstractTransform;
import io.landysh.inflor.java.core.transforms.BoundDisplayTransform;
import io.landysh.inflor.java.core.transforms.LogicleTransform;
import io.landysh.inflor.java.core.transforms.LogrithmicTransform;
import io.landysh.inflor.java.core.transforms.TransformType;

//Default serialization not used. We should measure performance.
@SuppressWarnings("serial")
public class FCSDimension extends DomainObject implements Comparable<FCSDimension>{
	
	//eg. the n in PnN
	private int parameterIndex;
	
	//$PnE Amplification type 
	private double ampTypef1;
	private double ampTypef2;
	
	//$PnN Short name 
	private String shortName;
	
	//$PnS Stain name 
	private String stainName;
	
	boolean compRef;
	
	//$PnR Range
	private double range;
	
	private double[] data;

	private AbstractTransform linearTransform;
	private AbstractTransform logTransform;
	private AbstractTransform logicleTransform;

	public FCSDimension(int size, int index, String pnn, 
			String pns, double pneF1, double pneF2, double pnr, boolean wasComped) {
		this(null,size,index,pnn,pns,pneF1,pneF2,pnr,wasComped);
	}
	
	public FCSDimension(String priorUUID, int size, int index, String pnn, 
			String pns, double pneF1, double pneF2, double pnr, boolean wasComped) {
		super(priorUUID);
		parameterIndex = index;
		shortName = pnn;
		stainName = pns;
		ampTypef1 = pneF1;
		ampTypef2 = pneF2;
		range = pnr;
		compRef = wasComped;
		this.data = new double[size] ;
		this.linearTransform = new BoundDisplayTransform(0, range);
		if (ampTypef1 ==0&&ampTypef2==0){
			this.logTransform = new LogrithmicTransform(1, Math.log10(range));
		} else {
			this.logTransform = new LogrithmicTransform(Math.exp(pneF1), Math.exp(pneF2));
		}
		this.logicleTransform = new LogicleTransform(ChartingDefaults.BIN_COUNT);
		
	}

	public double[] getData() {
		return data;
	}

	public int getSize() {
		return this.data.length;
	}

	public String getDisplayName(){
		if (compRef==true){
			if (stainName!=null){
				return "[" + shortName +": " + stainName + "]"; 
			} else {
				return "[" + shortName + "]";
			}
		} else {
			if (stainName!=null&&stainName!=""){
				return shortName +": " + stainName; 
			} else {
				return shortName;
			}
		}
	}

	@Override
	public int compareTo(FCSDimension other) {
		int result = 0;	
		if (parameterIndex<parameterIndex){
			result-=1;
		} else if (parameterIndex>parameterIndex){
			result+=1;
		} else {
			result = 0;
		}
	return result;
	}

	public int getIndex() {
		return this.parameterIndex;
	}

	public String getShortName() {
		return this.shortName;
	}

	public String getStainName() {
		return this.stainName;
	}

	public double getPNEF1() {
		return this.ampTypef1;
	}

	public double getPNEF2() {
		return this.ampTypef2;
	}

	public double getRange() {
		return this.range;
	}

	public boolean getCompRef() {
		return this.compRef;
	}

	public void setData(double[] newData) {
		this.data = newData;
	}
	
	@Override
	public String toString(){
		return this.getDisplayName();
	}

	public AbstractTransform getPreferredTransform() {
		String[] regi = ParameterTypes.SCATTER.regi();
		for (String regex:regi){
			String name = shortName.toLowerCase();
			if (name.matches(regex)){
				return this.linearTransform;
			}
		}
		return this.logicleTransform;
	}

	public AbstractTransform getTransform(TransformType selectedType) {
		if (selectedType==TransformType.Linear){
			return linearTransform;
		} else if (selectedType==TransformType.Logrithmic){
			return logTransform;
		} else if (selectedType==TransformType.Logicle){
			return logicleTransform;
		} else {
			System.out.println("CODING ERROR");//This should never happen.
			throw new IllegalArgumentException("Transform type: " + selectedType + " not supported."); 
		}
	}
}
