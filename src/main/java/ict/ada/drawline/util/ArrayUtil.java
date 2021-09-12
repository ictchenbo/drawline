package ict.ada.drawline.util;

import java.util.Arrays;

/**
 * Helper functions for array.
 *
 * @author Jiyuan Lin
 */
public final class ArrayUtil {
  private ArrayUtil() {
  }

  /**
   * Find the index of an object among an array.
   *
   * @param arr the array to search
   * @param o   the target object
   */
  public static int indexOf(Object[] arr, Object o) {
    for (int i = 0; i < arr.length; ++i) {
      if (arr[i] == null && o == null) {
        return i;
      }
      if (arr[i] != null && arr[i].equals(o)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Unique a sorted int array.
   * If it's mutable, the original array will be used instead of creating a new temp one.
   *
   * @param arr     the sorted int array to be unique
   * @param mutable whether the array is mutable or not
   */
  public static int[] unique(int[] arr, boolean mutable) {
    int[] result = mutable ? arr.clone() : arr;
    int m = 0;
    for (int i = 0; i < arr.length; ++i) {
      if (i == 0 || arr[i] != arr[i - 1]) {
        result[m++] = arr[i];
      }
    }
    return Arrays.copyOf(result, m);
  }

  /**
   * An overloaded version of unqiue function where the array is mutable.
   *
   * @param arr the sorted int array to be unique
   */
  public static int[] unique(int[] arr) {
    return unique(arr, true);
  }
}
