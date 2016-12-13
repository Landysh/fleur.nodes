package io.landysh.inflor.main.core.gates;

public abstract class AbstractGMLDimension {
  private final String name;

  public AbstractGMLDimension(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
