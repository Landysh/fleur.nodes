package io.landysh.inflor.java.core.transforms;

import java.io.Serializable;

import io.landysh.inflor.java.core.dataStructures.DomainObject;

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
