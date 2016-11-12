package io.landysh.inflor.java.core.fcs;

public enum ParameterTypes {
  ForwardScatter(new String[] {".*fsc.*", ".*fcs.*", ".*forward.*", ".*size.*"}),
  SideScatter(new String[] {".*side.*", ".*ssc.*", ".*orth.*"}),
  VIABILITY(new String[] {}), 
  TIME(new String[] {".*time.*"}), 
  DNA(new String[] {".*dapi.*", ".*pi.*"});

  private final String[] regi;

  ParameterTypes(String[] regi) {
    this.regi = regi;
  }

  public boolean matches(String parameterName) {
    for (String regex : this.regi) {
      if (parameterName.toLowerCase().matches(regex)) {
        return true;
      };
    }
    return false;
  }
}
