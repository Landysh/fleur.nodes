package io.landysh.inflor.java.core.gates;

public abstract class AbstractGMLDimension {
  private final String name;
  private String compensationRef;
  private String transformationRef;

  public AbstractGMLDimension(final String name) {
    this.name = name;
  }

  public String getCompensationRef() {
    return compensationRef;
  }

  public String getName() {
    return name;
  }

  public String getTransformationRef() {
    return transformationRef;
  }

  public void setCompensationRef(String compensationRef) {
    this.compensationRef = compensationRef;
  }

  public void setTransformationRef(String transformationRef) {
    this.transformationRef = transformationRef;
  }
}
