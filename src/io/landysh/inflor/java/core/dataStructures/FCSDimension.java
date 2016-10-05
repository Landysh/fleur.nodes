package io.landysh.inflor.java.core.dataStructures;

//Default serialization not used on suspicion of performance issues. We should measure this...
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
		compRef = wasComped;
		this.data = new double[size] ;
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
			if (stainName!=null){
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
	
}
