package io.landysh.inflor.main.core.singlets;

public enum PuleProperties {
  AREA(new String[] {".+-a", ".+area.+", ".+-a>",  ".+-a]"}), 
  HEIGHT(new String[] {".+-h", ".+height.+", ".+-h>",  ".+-h]"}),
  WIDTH(new String[] {".+-w", ".+width.+", ".+-w>", ".+-w]"});

 
  private final String[] regi;

  PuleProperties(String[] regi) {
    this.regi = regi;
  }

  public String[] regi() {
    return regi;
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
