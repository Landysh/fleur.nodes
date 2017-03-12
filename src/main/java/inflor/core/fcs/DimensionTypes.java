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
package inflor.core.fcs;

public enum DimensionTypes {
  FORWARD_SCATTER(new String[] {".*fsc.*", ".*fcs.*", ".*forward.*", ".*size.*"}),
  SIDE_SCATTER(new String[] {".*side.*", ".*ssc.*", ".*orth.*"}),
  VIABILITY(new String[] {}), 
  TIME(new String[] {".*time.*"}), 
  DNA(new String[] {".*dapi.*", ".*pi.*"});

  private final String[] regi;

  DimensionTypes(String[] regi) {
    this.regi = regi;
  }

  public boolean matches(String parameterName) {
    for (String regex : this.regi) {
      if (parameterName.toLowerCase().matches(regex)) {
        return true;
      }
    }
    return false;
  }
}
