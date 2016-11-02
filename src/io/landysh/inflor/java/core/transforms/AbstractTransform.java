package io.landysh.inflor.java.core.transforms;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractTransform implements Serializable, Cloneable {
  // TODO: no longer any fields, should be an interface?
  abstract public double[] transform(double[] rawData);

  abstract public double transform(double value);

  abstract public double inverse(double value);

  abstract public double getMinTranformedValue();

  abstract public double getMaxTransformedValue();

  abstract public double getMinRawValue();

  abstract public double getMaxRawValue();
}
