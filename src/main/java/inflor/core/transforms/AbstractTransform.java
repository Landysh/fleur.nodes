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
package main.java.inflor.core.transforms;

import java.io.Serializable;

import main.java.inflor.core.data.DomainObject;

@SuppressWarnings("serial")
public abstract class AbstractTransform extends DomainObject implements Serializable, Cloneable {
  
  public AbstractTransform(String priorUUID) {
    super(priorUUID);
  }

  public abstract double[] transform(double[] rawData);

  public abstract double transform(double value);

  public abstract double inverse(double value);

  public abstract double getMinTranformedValue();

  public abstract double getMaxTransformedValue();

  public abstract double getMinRawValue();

  public abstract double getMaxRawValue();
}
