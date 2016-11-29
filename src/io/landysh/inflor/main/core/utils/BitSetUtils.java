package io.landysh.inflor.main.core.utils;

import java.text.NumberFormat;
import java.util.BitSet;
import java.util.Random;

public class BitSetUtils {

  public static String frequencyOfParent(BitSet set, int maxDecimalDigits) {
    double num = set.cardinality();
    double denom = set.size();
    double result = num / denom;
    NumberFormat formatter = NumberFormat.getPercentInstance();
    formatter.setMaximumFractionDigits(maxDecimalDigits);
    return formatter.format(result);
  }

  public static BitSet getShuffledMask(int inSize, int downSize) {
    /**
     * Based on a knuth shuffle https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle#
     * The_modern_algorithm
     */
    // make an array of indices
    final int[] indices = new int[inSize];
    for (int i = 0; i < inSize; i++) {
      indices[i] = i;
    }
    // Init random number
    final Random rand = new Random(-1);
    final BitSet mask = new BitSet(inSize);
    // The knuthy part
    for (int i = 0; i < downSize; i++) {
      final int pos = i + rand.nextInt(inSize - i);
      final int temp = indices[pos];
      indices[pos] = indices[i];
      indices[i] = temp;
      mask.set(i);
    }
    return mask;
  }

  public static double[] filter(double[] data, BitSet mask) {
    double[] result = new double[mask.cardinality()];
    int j = 0;
    for (int i = 0; i < data.length; i++) {
      if (mask.get(i)) {
        result[j] = data[i];
        j++;
      }
    }
    return result;
  }
}
