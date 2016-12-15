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
package io.landysh.inflor.main.core.viability;

public enum ViabilityDyes {
  PI(new String[] {".+prop.+", ".+PI.+"}), 
  Calcein(new String[] {".+Calcein.+",}), 
  eFluor(new String[] {".+eFluor.+"}), 
  Horizon(new String[] {".+Horizon.+"});

  private final String[] regi;

  ViabilityDyes(String[] regi) {
    this.regi = regi;
  }

  public String[] regi() {
    return regi;
  }
}
