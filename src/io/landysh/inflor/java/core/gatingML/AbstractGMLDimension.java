package io.landysh.inflor.java.core.gatingML;

public abstract class AbstractGMLDimension {
	private String name;
	private String compensationRef;
	private String transformationRef;
	
	public AbstractGMLDimension(final String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}

	public String getCompensationRef() {
		return compensationRef;
	}

	public void setCompensationRef(String compensationRef) {
		this.compensationRef = compensationRef;
	}

	public String getTransformationRef() {
		return transformationRef;
	}

	public void setTransformationRef(String transformationRef) {
		this.transformationRef = transformationRef;
	}
}
