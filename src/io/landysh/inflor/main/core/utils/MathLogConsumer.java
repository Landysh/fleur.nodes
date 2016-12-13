package io.landysh.inflor.main.core.utils;

import java.util.function.DoubleConsumer;

public class MathLogConsumer implements DoubleConsumer {

  @Override
  public void accept(double value) {
    value = Math.log10(value);
  }

}
