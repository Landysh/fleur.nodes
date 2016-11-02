package io.landysh.inflor.java.core.viability;

public enum ViabilityDyes {
  PI(new String[] {".+prop.+", ".+PI.+"}), Calcein(new String[] {".+Calcein.+",}), eFluor(
      new String[] {".+eFluor.+"}), Horizon(new String[] {".+Horizon.+"});

  private final String[] regi;

  ViabilityDyes(String[] regi) {
    this.regi = regi;
  }

  public String[] regi() {
    return regi;
  }
}
