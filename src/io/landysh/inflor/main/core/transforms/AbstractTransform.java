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
package io.landysh.inflor.main.core.transforms;

import java.io.Serializable;

import io.landysh.inflor.main.core.dataStructures.DomainObject;

@SuppressWarnings("serial")
public abstract class AbstractTransform extends DomainObject implements Serializable, Cloneable {
  
  public AbstractTransform(String priorUUID) {
    super(priorUUID);
  }

  abstract public double[] transform(double[] rawData);

  abstract public double transform(double value);

  abstract public double inverse(double value);

  abstract public double getMinTranformedValue();

  abstract public double getMaxTransformedValue();

  abstract public double getMinRawValue();

  abstract public double getMaxRawValue();
}
