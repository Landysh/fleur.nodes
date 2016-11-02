package io.landysh.inflor.java.core.singlets;

public enum PuleProperties {
  AREA(new String[] {".+-A", ".+Area.+", ".+-A>"}), HEIGHT(
      new String[] {".+-H", ".+Height.+", ".+-H>"}), WIDTH(
          new String[] {".+-W", ".+Width.+", ".+-W>"});
  private final String[] regi;

  PuleProperties(String[] regi) {
    this.regi = regi;
  }

  public String[] regi() {
    return regi;
  }
}
