/*
 * ------------------------------------------------------------------------
 *  Copyright 2016 by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 14, 2016 by Aaron Hart
 */
package fleur.core.singlets;

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
