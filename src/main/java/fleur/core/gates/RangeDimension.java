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
package fleur.core.gates;

import java.io.Serializable;
import java.util.BitSet;

@SuppressWarnings("serial")
public class RangeDimension implements Serializable{

  public double min = Double.MIN_VALUE;
  public double max = Double.MAX_VALUE;
  private String name;

  public RangeDimension(String name, double min, double max) {
    this.name = name;
    this.min = min;
    this.max = max;
  }
  
//  public RangeDimension() { }

  public BitSet evaluate(double[] data) {
    BitSet result = new BitSet(data.length);
    for (int i = 0; i < data.length; i++) {
      if (min <= data[i] && data[i] < max) {
        result.set(i);
      }
    }
    return result;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
